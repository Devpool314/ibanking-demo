package com.example.tuition_system.model.dto;

public record ApiResponse(String message, Object data) {
    public ApiResponse(String message) {
        this(message, null);
    }
}