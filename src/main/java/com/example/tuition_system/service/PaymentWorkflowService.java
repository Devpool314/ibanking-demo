package com.example.tuition_system.service;

import com.example.tuition_system.model.*;
import com.example.tuition_system.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentWorkflowService {

    private final PaymentRepository paymentRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final TuitionInfoRepository tuitionInfoRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    public PaymentWorkflowService(PaymentRepository paymentRepository,
                                  PaymentRequestRepository paymentRequestRepository,
                                  TuitionInfoRepository tuitionInfoRepository,
                                  UserRepository userRepository,
                                  MailService mailService) {
        this.paymentRepository = paymentRepository;
        this.paymentRequestRepository = paymentRequestRepository;
        this.tuitionInfoRepository = tuitionInfoRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    public void processConfirmed(Long transactionId, String confirmedBy) {
        // Lock và kiểm tra trạng thái PaymentRequest
        PaymentRequest pr = paymentRequestRepository.lockById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("PaymentRequest not found"));
                
        if (pr.getStatus() != PaymentRequest.Status.PENDING) {
            throw new IllegalStateException("Payment request is not in PENDING state");
        }

        // Lock và kiểm tra MSSV có đang được thanh toán bởi request khác không
        TuitionInfo tuition = tuitionInfoRepository.lockByMssv(pr.getMssv()).orElse(null);
        if (tuition != null && tuition.isPaid()) {
            pr.setStatus(PaymentRequest.Status.EXPIRED);
            paymentRequestRepository.save(pr);
            throw new IllegalStateException("Tuition fee was already paid by another transaction");
        }

        // Lock và kiểm tra số dư người nộp
        User payer = userRepository.lockByUsername(pr.getPayerUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (payer.getBalance() < pr.getAmount()) {
            pr.setStatus(PaymentRequest.Status.EXPIRED);
            paymentRequestRepository.save(pr);
            throw new IllegalStateException("Insufficient balance at confirm step");
        }

        // Trừ tiền và cập nhật trạng thái
        double newBalance = payer.getBalance() - pr.getAmount();
        payer.setBalance(newBalance);
        userRepository.save(payer);

        if (tuition != null) {
            tuition.setPaid(true);
            tuitionInfoRepository.save(tuition);
        }

        Payment payment = new Payment();
        payment.setMssv(pr.getMssv());
        payment.setAmount(pr.getAmount());
        payment.setPayerUsername(pr.getPayerUsername());
        payment.setAvailableBalance(newBalance);
        payment.setPaid(true);
        payment.setStatus("SUCCESS");
        payment.setPaymentDate(java.time.LocalDateTime.now());
        paymentRepository.save(payment);

        // Cập nhật trạng thái request thành công
        pr.setStatus(PaymentRequest.Status.CONFIRMED);
        paymentRequestRepository.save(pr);

        String studentName = (tuition != null) ? tuition.getFullname() : null;
        mailService.sendPaymentSuccess(
                pr.getPayerEmail(),
                pr.getMssv(),
                studentName,
                pr.getAmount(),
                payment.getId().toString(),
                newBalance
        );
    }
}