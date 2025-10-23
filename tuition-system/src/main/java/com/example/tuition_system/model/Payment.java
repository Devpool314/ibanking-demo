package com.example.tuition_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mssv;
    private double amount;

    private double availableBalance;
    private double tuitionFee;
    private boolean termsAccepted = false;
    private boolean paid;
    private String status;

    private LocalDateTime paymentDate;
    
    

    @Column(name = "payer_username", nullable = false)
    private String payerUsername; 

    // thêm 2 field:
    @Column(name = "correlation_id", unique = true)
    private String correlationId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Payment() {
        this.paymentDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Payment(String mssv, double amount, String payerUsername) {
        this.mssv = mssv;
        this.amount = amount;
        this.payerUsername = payerUsername;
        this.paymentDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    

    // getters / setters

    public String getPayerUsername() { return payerUsername; }
    public void setPayerUsername(String payerUsername) { this.payerUsername = payerUsername; }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMssv() { return mssv; }
    public void setMssv(String mssv) { this.mssv = mssv; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(double availableBalance) { this.availableBalance = availableBalance; }

    public double getTuitionFee() { return tuitionFee; }
    public void setTuitionFee(double tuitionFee) { this.tuitionFee = tuitionFee; }

    public boolean isTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}