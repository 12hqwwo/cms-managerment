package com.project.WebAloTra.service.serviceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.VerificationCode;
import com.project.WebAloTra.exception.ShopApiException;
import com.project.WebAloTra.repository.AccountRepository;
import com.project.WebAloTra.repository.VerificationRepository;
import com.project.WebAloTra.service.EmailService;
import com.project.WebAloTra.service.VerificationCodeService;
import com.project.WebAloTra.utils.RandomUtils;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationRepository verificationRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;

    public VerificationCodeServiceImpl(VerificationRepository verificationRepository,
                                       AccountRepository accountRepository,
                                       EmailService emailService) {
        this.verificationRepository = verificationRepository;
        this.accountRepository = accountRepository;
        this.emailService = emailService;
    }

    /**
     * ✅ Gửi mã xác nhận (OTP) tới email người dùng.
     * Mã có hiệu lực trong 15 phút.
     */
    @Override
    @Transactional
    public VerificationCode createVerificationCode(String email) throws MessagingException {
        // Kiểm tra tài khoản tồn tại
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Không tìm thấy tài khoản với email này!");
        }

        // Xóa OTP cũ (nếu có)
        verificationRepository.findByAccount(account).ifPresent(verificationRepository::delete);

        // Sinh mã OTP mới (6 ký tự ngẫu nhiên)
        String verificationCodeValue = RandomUtils.randomAlphanumeric(6);
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15);

        // Tạo bản ghi OTP
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setAccount(account);
        verificationCode.setCode(verificationCodeValue);
        verificationCode.setExpiryTime(expiryTime);
        verificationRepository.save(verificationCode);

        // Gửi email xác nhận
        String subject = "Mã xác thực tài khoản Trà Sữa";
        StringBuilder content = new StringBuilder();
        content.append("<p>Xin chào,</p>");
        content.append("<p>Mã xác thực của bạn là: <b>")
               .append(verificationCodeValue)
               .append("</b></p>");
        content.append("<p>Mã có hiệu lực trong vòng 15 phút.</p>");
        content.append("<p>Trân trọng,<br>Đội ngũ AloTrà</p>");

        emailService.sendEmail(account.getEmail(), subject, content.toString());

        return verificationCode;
    }

    /**
     * ✅ Kiểm tra mã xác nhận hợp lệ (chưa hết hạn và đúng người dùng)
     */
    @Override
    public Account verifyCode(String code) {
        Optional<VerificationCode> optionalCode = verificationRepository.findByCode(code);

        if (optionalCode.isEmpty()) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã xác nhận không tồn tại hoặc đã được sử dụng!");
        }

        VerificationCode verificationCode = optionalCode.get();

        if (!isValid(verificationCode)) {
            verificationRepository.delete(verificationCode); // Xóa mã hết hạn
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã xác nhận đã hết hạn, vui lòng yêu cầu lại!");
        }

        // Nếu hợp lệ → xóa mã sau khi xác thực thành công
        Account account = verificationCode.getAccount();
        verificationRepository.delete(verificationCode);
        return account;
    }

    /**
     * ✅ Kiểm tra mã còn hạn không
     */
    private boolean isValid(VerificationCode verificationCode) {
        return verificationCode.getExpiryTime().isAfter(LocalDateTime.now());
    }
}
