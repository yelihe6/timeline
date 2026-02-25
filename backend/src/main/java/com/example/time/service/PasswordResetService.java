package com.example.time.service;

import com.example.time.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int CODE_LENGTH = 6;

    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    public String sendResetCode(String email, User user) {
        if (user == null) {
            return "该邮箱未注册";
        }
        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            return "邮件服务未配置，请联系管理员";
        }
        String code = generateCode();
        codeStore.put(email.trim().toLowerCase(), new CodeEntry(code, System.currentTimeMillis() + CODE_EXPIRE_MINUTES * 60 * 1000));
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(email.trim());
            msg.setSubject("时间线 - 找回密码验证码");
            msg.setText("您的验证码是：" + code + "，有效期" + CODE_EXPIRE_MINUTES + "分钟，请勿泄露。");
            mailSender.send(msg);
            return null;
        } catch (Exception e) {
            codeStore.remove(email.trim().toLowerCase());
            return "发送失败：" + e.getMessage();
        }
    }

    /**
     * 仅验证验证码是否正确，不移除（用于前端分步验证）
     */
    public boolean verifyCode(String email, String code) {
        String key = email.trim().toLowerCase();
        CodeEntry entry = codeStore.get(key);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expiry) return false;
        return entry.code.equals(code);
    }

    public boolean verifyAndReset(String email, String code, String newPassword) {
        String key = email.trim().toLowerCase();
        CodeEntry entry = codeStore.get(key);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expiry) {
            codeStore.remove(key);
            return false;
        }
        if (!entry.code.equals(code)) return false;
        codeStore.remove(key);
        return true;
    }

    private String generateCode() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }

    private static class CodeEntry {
        final String code;
        final long expiry;

        CodeEntry(String code, long expiry) {
            this.code = code;
            this.expiry = expiry;
        }
    }
}
