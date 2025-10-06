package com.example.tuition_system.controller;

import com.example.tuition_system.model.Payment;
import com.example.tuition_system.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {

    private final PaymentService service;

    public PaymentRestController(PaymentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Payment> getPayments() {
        return service.listAll();
    }

    @PostMapping
    public Payment createPayment(@RequestParam String mssv,
                                 @RequestParam double amount) {
        Payment p = new Payment();
        p.setMssv(mssv);
        p.setAmount(amount);
        return service.process(p);
    }
}
