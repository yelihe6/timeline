package com.example.time.service;

import com.example.time.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 测试邮件发送是否成功
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private PasswordResetService passwordResetService;

    private static final String TEST_EMAIL = "1665917148@qq.com";
    private static final String FROM_EMAIL = "yelihe6@gmail.com";

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService();
        ReflectionTestUtils.setField(passwordResetService, "mailSender", mailSender);
        ReflectionTestUtils.setField(passwordResetService, "fromEmail", FROM_EMAIL);
    }

    @Test
    void sendResetCode_shouldSendEmailSuccessfully() {
        User user = new User(1, TEST_EMAIL, "测试用户", null, null, 0);

        String result = passwordResetService.sendResetCode(TEST_EMAIL, user);

        assertNull(result, "发送成功应返回 null");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(FROM_EMAIL, sentMessage.getFrom());
        assertArrayEquals(new String[]{TEST_EMAIL}, sentMessage.getTo());
        assertEquals("时间线 - 找回密码验证码", sentMessage.getSubject());
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains("您的验证码是："));
        assertTrue(sentMessage.getText().contains("，有效期5分钟"));
        assertTrue(sentMessage.getText().matches(".*\\d{6}.*"), "邮件正文应包含6位数字验证码");
    }

    @Test
    void sendResetCode_whenUserNull_shouldReturnError() {
        String result = passwordResetService.sendResetCode(TEST_EMAIL, null);

        assertEquals("该邮箱未注册", result);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendResetCode_whenMailSenderNull_shouldReturnError() {
        ReflectionTestUtils.setField(passwordResetService, "mailSender", null);
        User user = new User(1, TEST_EMAIL, "测试用户", null, null, 0);

        String result = passwordResetService.sendResetCode(TEST_EMAIL, user);

        assertEquals("邮件服务未配置，请联系管理员", result);
    }

    @Test
    void sendResetCode_whenFromEmailBlank_shouldReturnError() {
        ReflectionTestUtils.setField(passwordResetService, "fromEmail", "");
        User user = new User(1, TEST_EMAIL, "测试用户", null, null, 0);

        String result = passwordResetService.sendResetCode(TEST_EMAIL, user);

        assertEquals("邮件服务未配置，请联系管理员", result);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendResetCode_whenMailSenderThrows_shouldReturnErrorAndRemoveCode() {
        doThrow(new RuntimeException("SMTP 连接失败")).when(mailSender).send(any(SimpleMailMessage.class));
        User user = new User(1, TEST_EMAIL, "测试用户", null, null, 0);

        String result = passwordResetService.sendResetCode(TEST_EMAIL, user);

        assertNotNull(result);
        assertTrue(result.contains("发送失败"));
        assertTrue(result.contains("SMTP 连接失败"));

        // 验证码应从 store 中移除
        assertFalse(passwordResetService.verifyAndReset(TEST_EMAIL, "123456", "newPass123!"));
    }

    @Test
    void verifyAndReset_afterSendCode_shouldSucceed() {
        User user = new User(1, TEST_EMAIL, "测试用户", null, null, 0);
        passwordResetService.sendResetCode(TEST_EMAIL, user);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        String text = captor.getValue().getText();
        String code = text.split("：")[1].split("，")[0];

        boolean ok = passwordResetService.verifyAndReset(TEST_EMAIL, code, "NewPass123!");
        assertTrue(ok);
    }
}
