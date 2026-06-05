package com.project.WebAloTra.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.VerificationCode;
import com.project.WebAloTra.service.VerificationCodeService;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/api/verification")
public class VerificationCodeController {

    private final VerificationCodeService verificationCodeService;

    public VerificationCodeController(VerificationCodeService verificationCodeService) {
        this.verificationCodeService = verificationCodeService;
    }

    /** ✅ Gửi mã xác nhận (OTP) đến email */
    @PostMapping("/send")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String email) throws MessagingException {
        VerificationCode code = verificationCodeService.createVerificationCode(email);
        return ResponseEntity.ok("✅ Mã xác thực đã được gửi đến " + email + " (hiệu lực 15 phút)");
    }

    /** ✅ Xác minh mã OTP */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestParam String code) {
        Account account = verificationCodeService.verifyCode(code);
        if (account == null) {
            return ResponseEntity.badRequest().body("❌ Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
        return ResponseEntity.ok("✅ Xác thực thành công cho tài khoản: " + account.getEmail());
    }
}
