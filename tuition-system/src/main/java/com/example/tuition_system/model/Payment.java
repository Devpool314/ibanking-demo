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
    private LocalDateTime paymentDate;
    
    

    @Column(name = "payer_username", nullable = false)
    private String payerUsername; 

    public Payment() {
        // khởi tạo ngày mặc định (an toàn khi tạo object mới từ controller)
        this.paymentDate = LocalDateTime.now();
    }

    public Payment(String mssv, double amount, String payerUsername) {
        this.mssv = mssv;
        this.amount = amount;
        this.payerUsername = payerUsername;
        this.paymentDate = LocalDateTime.now();
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

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}