package com.example.tuition_system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tuition_info")
public class TuitionInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String mssv;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private boolean paid = false; // false = chưa đóng, true = đã đóng

    // Constructors
    public TuitionInfo() {}

    public TuitionInfo(String mssv, String fullname, double amount) {
        this.mssv = mssv;
        this.fullname = fullname;
        this.amount = amount;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getMssv() { return mssv; }
    public void setMssv(String mssv) { this.mssv = mssv; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}
