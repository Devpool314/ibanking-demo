package com.example.tuition_system.model.dto;

// Dùng record cho ngắn gọn, hoặc tạo class Java thường với getters/setters
public record StudentDto(String mssv, String name, double tuitionFee) {
}