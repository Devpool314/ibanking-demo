package com.example.tuition_system.service;

import com.example.tuition_system.model.OtpVerification;
import com.example.tuition_system.repository.OtpVerificationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpService {

    private final OtpVerificationRepository otpRepo;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public OtpService(OtpVerificationRepository otpRepo,
                      PasswordEncoder passwordEncoder,
                      MailService mailService) {
        this.otpRepo = otpRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    public String sendOtp(Long transactionId, String email) {
        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));

        OtpVerification ov = new OtpVerification();
        ov.setTransactionId(transactionId);
        ov.setEmail(email == null ? "" : email);
        ov.setOtpHash(passwordEncoder.encode(otp));
        ov.setAttempts(0);
        ov.setStatus(OtpVerification.Status.PENDING);
        ov.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        ov.setCreatedAt(LocalDateTime.now());
        otpRepo.save(ov);

        if (email != null && !email.isBlank()) {
            String subject = "Mã OTP xác nhận thanh toán";
            String html = """
                <div style="font-family:Arial,sans-serif">
                  <p>Chào bạn,</p>
                  <p>Mã OTP của bạn là: <b style="font-size:18px;">%s</b></p>
                  <p>OTP có hiệu lực trong <b>5 phút</b>.</p>
                </div>
                """.formatted(otp);
            mailService.sendHtml(email, subject, html);
        } else {
            System.err.println("[OTP][WARN] Email trống, bỏ qua gửi mail. OTP=" + otp);
        }

        return otp; 
    }

    public boolean verify(Long transactionId, String rawOtp) {
        OtpVerification ov =
                otpRepo.findTopByTransactionIdOrderByIdDesc(transactionId).orElse(null);
        if (ov == null || ov.getStatus() != OtpVerification.Status.PENDING) return false;

        if (LocalDateTime.now().isAfter(ov.getExpiresAt())) {
            ov.setStatus(OtpVerification.Status.EXPIRED);
            otpRepo.save(ov);
            return false;
        }
        if (ov.getAttempts() >= 5) return false;

        ov.setAttempts(ov.getAttempts() + 1);
        boolean ok = passwordEncoder.matches(rawOtp, ov.getOtpHash());
        if (ok) ov.setStatus(OtpVerification.Status.VERIFIED);
        otpRepo.save(ov);
        return ok;
    }
}
