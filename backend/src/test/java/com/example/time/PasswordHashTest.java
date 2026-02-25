package com.example.time;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 测试用：生成加密后的密码，用于手动更新数据库中的明文密码
 * 运行方式：mvn test -Dtest=PasswordHashTest
 * 或在 IDE 中右键运行此类
 */
class PasswordHashTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void generateHashedPassword() {
        String password = "yelihe666_";  // 修改此处可生成不同密码的加密结果
        String hashed = passwordEncoder.encode(password);
        System.out.println("========================================");
        System.out.println("明文密码: " + password);
        System.out.println("加密结果: " + hashed);
        System.out.println("========================================");
    }
}
