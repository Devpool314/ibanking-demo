package com.example.tuition_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification")
public class OtpVerification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)        private String email;
    @Column(nullable = false)        private String otpHash;     // BCrypt
    @Column(nullable = false)        private Integer attempts = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)        private Status status;      // PENDING/VERIFIED/EXPIRED/FAILED

    @Column(name="transaction_id", nullable = false)
    private Long transactionId;                                   // FK -> payment_requests.id

    @Column(name="expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name="created_at")       private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status { PENDING, VERIFIED, EXPIRED, FAILED }

    // getters/setters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
