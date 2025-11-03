package com.example.tuition_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "redirect:/web/login";
    }

    @GetMapping("/web/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/web/payments")
    public String paymentListPage() {
        return "payment-list";
    }
    
    @GetMapping("/web/pay")
    public String paymentPage() {
        return "payment"; 
    }

    @GetMapping("/web/verify")
    public String verifyPage() {
        return "transaction-otp";
    }
}