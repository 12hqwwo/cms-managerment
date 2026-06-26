package com.project.WebAloTra.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactController {

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("getcontact")
    public String getContact(Model model) {
        return "user/contact";
    }

    @PostMapping("getcontact")
    public String sendContact(
            @RequestParam("email") String email,
            @RequestParam("msg") String message,
            Model model) {

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo("duyc080807@gmail.com"); // email của cửa hàng
            mail.setSubject("Đề xuất từ khách hàng");
            mail.setText("Từ: " + email + "\n\nNội dung:\n" + message);
            mailSender.send(mail);

            model.addAttribute("success", "Gửi thành công! Cảm ơn bạn đã liên hệ.");
        } catch (Exception e) {
            model.addAttribute("error", "Gửi thất bại, vui lòng thử lại sau!");
        }

        return "user/contact";
    }
}
