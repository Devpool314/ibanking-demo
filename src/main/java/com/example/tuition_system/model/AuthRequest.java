package com.example.tuition_system.model;

// Dùng record cho ngắn gọn, hoặc tạo class Java bình thường
public record AuthRequest(String username, String password) {
}