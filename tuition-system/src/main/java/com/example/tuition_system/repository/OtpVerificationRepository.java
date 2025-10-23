package com.example.tuition_system.repository;

import com.example.tuition_system.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByTransactionIdOrderByIdDesc(Long transactionId);
}
