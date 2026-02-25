package com.example.time.controller;

import com.example.time.pojo.ApiResult;
import com.example.time.pojo.User;
import com.example.time.service.PasswordResetService;
import com.example.time.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ApiResult<User> login(@RequestBody Map<String, String> params, HttpSession session) {
        String email = params.get("email");
        String password = params.get("password");
        String verifyCode = params.get("verifyCode");

        // 验证码检查优先于邮箱密码
        if (verifyCode == null || verifyCode.isBlank()) {
            return ApiResult.error("请输入验证码");
        }

        Object sessionCaptcha = session.getAttribute("verifyCode");
        if (sessionCaptcha == null) {
            return ApiResult.error("验证码超时");
        }

        String code = sessionCaptcha.toString().toLowerCase();
        if (!verifyCode.trim().toLowerCase().equals(code)) {
            return ApiResult.error("验证码错误");
        }

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ApiResult.error("邮箱和密码不能为空");
        }

        User user = userService.login(email, password);
        if (user == null) {
            return ApiResult.error(401, "账号或密码错误");
        }
        return ApiResult.success(user);
    }

    @PostMapping("/register")
    public ApiResult<String> register(@RequestBody Map<String, String> params, HttpSession session) {
        String email = params.get("email");
        String password = params.get("password");
        String verifyCode = params.get("verifyCode");

        if (email == null || email.isBlank()) {
            return ApiResult.error("邮箱不能为空");
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ApiResult.error("邮箱格式不正确");
        }
        if (password == null || password.isBlank()) {
            return ApiResult.error("密码不能为空");
        }
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&._])[A-Za-z\\d@$!%*#?&._]{8,16}$")) {
            return ApiResult.error("密码需8-16位，且包含数字、英文字母和特殊字符");
        }
        if (verifyCode == null || verifyCode.isBlank()) {
            return ApiResult.error("请输入验证码");
        }

        Object sessionCaptcha = session.getAttribute("verifyCode");
        if (sessionCaptcha == null) {
            return ApiResult.error("验证码已过期，请刷新页面后重试");
        }

        String code = sessionCaptcha.toString().toLowerCase();
        if (!verifyCode.trim().toLowerCase().equals(code)) {
            return ApiResult.error("验证码错误");
        }

        User exist = userService.findByEmail(email);
        if (exist != null) {
            return ApiResult.error("该邮箱已注册，请使用其他邮箱");
        }

        userService.register(email, password);
        return ApiResult.success("注册成功");
    }

    @PutMapping("/user/{id}/nickname")
    public ApiResult<String> updateNickname(@PathVariable int id, @RequestBody Map<String, String> params) {
        String nickname = params.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            return ApiResult.error("昵称不能为空");
        }
        nickname = nickname.trim();
        if (nickname.length() > 50) {
            return ApiResult.error("昵称不能超过50个字符");
        }
        int n = userService.updateNickname(id, nickname);
        return n > 0 ? ApiResult.success("修改成功") : ApiResult.error("修改失败");
    }

    @PutMapping("/user/{id}/info")
    public ApiResult<String> updateUserInfo(@PathVariable int id, @RequestBody Map<String, String> params) {
        String nickname = params.get("nickname");
        String phone = params.get("phone");
        if (nickname == null || nickname.isBlank()) {
            return ApiResult.error("昵称不能为空");
        }
        nickname = nickname.trim();
        if (nickname.length() > 50) {
            return ApiResult.error("昵称不能超过50个字符");
        }
        if (phone != null && phone.trim().length() > 20) {
            return ApiResult.error("电话号码不能超过20个字符");
        }
        int n = userService.updateUserInfo(id, nickname, phone);
        return n > 0 ? ApiResult.success("修改成功") : ApiResult.error("修改失败");
    }

    @PutMapping("/user/{id}/password")
    public ApiResult<String> changePassword(@PathVariable int id, @RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || oldPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ApiResult.error("原密码和新密码不能为空");
        }
        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&._])[A-Za-z\\d@$!%*#?&._]{8,16}$")) {
            return ApiResult.error("新密码需8-16位，且包含数字、英文字母和特殊字符");
        }
        boolean ok = userService.changePassword(id, oldPassword, newPassword);
        return ok ? ApiResult.success("密码修改成功") : ApiResult.error("原密码错误");
    }

    /**
     * 检查邮箱是否已被注册
     */
    @GetMapping("/check-email")
    public ApiResult<Boolean> checkEmail(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            return ApiResult.success(true);
        }
        User exist = userService.findByEmail(email.trim());
        return ApiResult.success(exist == null);
    }

    /**
     * 找回密码 - 发送邮箱验证码（仅对已注册邮箱）
     */
    @PostMapping("/forgot-password/send-code")
    public ApiResult<String> sendResetCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        if (email == null || email.isBlank()) {
            return ApiResult.error("请输入邮箱");
        }
        email = email.trim();
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ApiResult.error("邮箱格式不正确");
        }
        User user = userService.findByEmail(email);
        if (user == null) {
            return ApiResult.error("该邮箱未注册");
        }
        String err = passwordResetService.sendResetCode(email, user);
        if (err != null) {
            return ApiResult.error(err);
        }
        return ApiResult.success("验证码已发送");
    }

    /**
     * 找回密码 - 验证验证码是否正确
     */
    @PostMapping("/forgot-password/verify-code")
    public ApiResult<String> verifyResetCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        if (email == null || email.isBlank()) {
            return ApiResult.error("请输入邮箱");
        }
        if (code == null || code.isBlank()) {
            return ApiResult.error("请输入验证码");
        }
        if (!passwordResetService.verifyCode(email.trim(), code.trim())) {
            return ApiResult.error("验证码错误或已过期");
        }
        return ApiResult.success("验证成功");
    }

    /**
     * 找回密码 - 验证码重置密码
     */
    @PostMapping("/forgot-password/reset")
    public ApiResult<String> resetPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");
        if (email == null || email.isBlank()) {
            return ApiResult.error("请输入邮箱");
        }
        if (code == null || code.isBlank()) {
            return ApiResult.error("请输入验证码");
        }
        if (newPassword == null || newPassword.isBlank()) {
            return ApiResult.error("请输入新密码");
        }
        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&._])[A-Za-z\\d@$!%*#?&._]{8,16}$")) {
            return ApiResult.error("新密码需8-16位，且包含数字、英文字母和特殊字符");
        }
        email = email.trim();
        if (!passwordResetService.verifyAndReset(email, code, newPassword)) {
            return ApiResult.error("验证码错误或已过期");
        }
        if (!userService.resetPasswordByEmail(email, newPassword)) {
            return ApiResult.error("重置失败");
        }
        return ApiResult.success("密码重置成功");
    }

    @GetMapping("/user/{id}")
    public ApiResult<User> getUser(@PathVariable int id) {
        User user = userService.findById(id);
        if (user == null) {
            return ApiResult.error("用户不存在");
        }
        return ApiResult.success(user);
    }
}
