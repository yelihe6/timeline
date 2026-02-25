package com.example.time.service;

import com.example.time.mapper.DataMapper;
import com.example.time.mapper.LoginMapper;
import com.example.time.mapper.RegisterMapper;
import com.example.time.pojo.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final LoginMapper loginMapper;
    private final RegisterMapper registerMapper;
    private final DataMapper dataMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(LoginMapper loginMapper, RegisterMapper registerMapper, DataMapper dataMapper) {
        this.loginMapper = loginMapper;
        this.registerMapper = registerMapper;
        this.dataMapper = dataMapper;
    }

    public User login(String email, String password) {
        User user = loginMapper.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    public User findById(int userId) {
        return dataMapper.userId(userId);
    }

    public User findByEmail(String email) {
        return registerMapper.findByEmail(email);
    }

    public int register(String email, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        String nickname = "用户" + (int) (Math.random() * 900000 + 100000);
        return registerMapper.userRegister(email, hashedPassword, nickname);
    }

    public int updateNickname(int userId, String nickname) {
        return dataMapper.updateNickname(userId, nickname);
    }

    public int updateUserInfo(int userId, String nickname, String phone) {
        return dataMapper.updateUserInfo(userId, nickname, phone != null ? phone.trim() : "");
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = dataMapper.userId(userId);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        String hashed = passwordEncoder.encode(newPassword);
        return dataMapper.updatePassword(userId, hashed) > 0;
    }

    public boolean resetPasswordByEmail(String email, String newPassword) {
        User user = registerMapper.findByEmail(email);
        if (user == null) return false;
        String hashed = passwordEncoder.encode(newPassword);
        return dataMapper.updatePassword(user.getUserId(), hashed) > 0;
    }

    /**
     * 测试用：生成加密后的密码
     */
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public int updateOpCount(int operationCount, int userId) {
        return dataMapper.opCount(operationCount, userId);
    }
}
