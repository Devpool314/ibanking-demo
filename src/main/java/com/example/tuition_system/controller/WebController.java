package com.example.tuition_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // Khi người dùng truy cập trang gốc, chuyển đến trang đăng nhập
    @GetMapping("/")
    public String index() {
        return "redirect:/web/login";
    }

    // Trả về file login.html
    @GetMapping("/web/login")
    public String loginPage() {
        return "login";
    }

    // Trả về file payment-list.html
    @GetMapping("/web/payments")
    public String paymentListPage() {
        return "payment-list";
    }
    
    // Trả về file payment.html
    @GetMapping("/web/pay")
    public String paymentPage() {
        return "payment"; 
    }

    // Trả về file transaction-otp.html
    @GetMapping("/web/verify")
    public String verifyPage() {
        return "transaction-otp";
    }
}