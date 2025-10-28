package com.example.tuition_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${app.mail.from:ibanking-demo@example.com}")
    private String fromAddress;

    @Async
    public void sendHtml(String to, String subject, String htmlBody) {
        if (!mailEnabled) {
            System.out.println("[MAIL] disabled → skip send to " + to);
            return;
        }
        try {
            MimeMessage mm = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mm, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(mm);
        } catch (MailException | jakarta.mail.MessagingException ex) {
            System.out.println("[MAIL] send failed: " + ex.getMessage());
        }
    }

    public void sendPaymentSuccess(String to, String mssv, String studentName,
                                   double amount, String txnId, double newBalance) {
        String subject = "Xác nhận giao dịch học phí thành công";
        String html = """
            <div style="font-family:Arial,sans-serif">
              <h3>Giao dịch thành công</h3>
              <p>Xin chào,</p>
              <p>Bạn đã thanh toán học phí cho sinh viên <b>%s (%s)</b>.</p>
              <ul>
                <li>Mã giao dịch: <b>%s</b></li>
                <li>Số tiền: <b>%,.0f VND</b></li>
                <li>Số dư mới: <b>%,.0f VND</b></li>
              </ul>
              <p>Cảm ơn bạn đã sử dụng dịch vụ.</p>
            </div>
            """.formatted(studentName == null ? "N/A" : studentName, mssv, txnId, amount, newBalance);
        sendHtml(to, subject, html);
    }
}
