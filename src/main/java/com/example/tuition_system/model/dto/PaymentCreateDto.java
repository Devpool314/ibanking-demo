package com.example.tuition_system.model.dto;

public record PaymentCreateDto(
    String mssv, 
    double amount, 
    boolean termsAccepted
) {
}