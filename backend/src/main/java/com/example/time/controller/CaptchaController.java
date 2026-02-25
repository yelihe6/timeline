package com.example.time.controller;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class CaptchaController {

    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/gif");

        SpecCaptcha captcha = new SpecCaptcha(150, 30, 4);
        captcha.setCharType(Captcha.FONT_9);

        HttpSession session = request.getSession();
        session.setAttribute("verifyCode", captcha.text().toLowerCase());

        captcha.out(response.getOutputStream());
    }
}
