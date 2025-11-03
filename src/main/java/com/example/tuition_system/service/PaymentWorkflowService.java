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
        PaymentRequest pr = paymentRequestRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("PaymentRequest not found"));

        // Trừ tiền người nộp
        User payer = userRepository.findByUsername(pr.getPayerUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (payer.getBalance() < pr.getAmount()) {
            throw new IllegalStateException("Insufficient balance at confirm step");
        }
        double newBalance = payer.getBalance() - pr.getAmount();
        payer.setBalance(newBalance);
        userRepository.save(payer);

        TuitionInfo tuition = tuitionInfoRepository.findByMssv(pr.getMssv()).orElse(null);
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