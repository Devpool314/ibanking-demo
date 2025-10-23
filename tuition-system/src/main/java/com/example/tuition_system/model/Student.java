package com.example.tuition_system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mssv;
    private String name;
    private double tuitionFee; // số tiền học phí cần nộp

    public Student() {}

    public Student(String mssv, String name, double tuitionFee) {
        this.mssv = mssv;
        this.name = name;
        this.tuitionFee = tuitionFee;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMssv() { return mssv; }
    public void setMssv(String mssv) { this.mssv = mssv; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTuitionFee() { return tuitionFee; }
    public void setTuitionFee(double tuitionFee) { this.tuitionFee = tuitionFee; }
}