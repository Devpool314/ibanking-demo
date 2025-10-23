package com.example.tuition_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        // Không bắt buộc nhưng an toàn hơn với Thymeleaf
        model.addAttribute("user", new Object());
        return "login";
    }
}