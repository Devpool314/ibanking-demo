package com.example.tuition_system.model.dto;

// Dùng để trả về thông báo cho Postman
public record ApiResponse(String message, Object data) {
    // Constructor đơn giản hơn chỉ cho message
    public ApiResponse(String message) {
        this(message, null);
    }
}