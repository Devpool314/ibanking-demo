package com.example.tuition_system.service;

import com.example.tuition_system.model.Payment;
import java.util.List;

public interface PaymentService {
    Payment process(Payment payment);
    List<Payment> listAll();
}
