package com.example.tuition_system; // Đảm bảo đúng package của bạn

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // Mật khẩu bạn muốn mã hóa
        String passwordToEncode = "admin123"; 
        
        System.out.println("Mật khẩu gốc: " + passwordToEncode);
        System.out.println("Mật khẩu đã mã hóa (BCrypt):");
        System.out.println(encoder.encode(passwordToEncode));
    }
}