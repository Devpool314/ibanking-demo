package com.example.tuition_system.controller;

import com.example.tuition_system.model.Payment;
import com.example.tuition_system.repository.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Hiển thị danh sách
    @GetMapping("/list")
    public String listPayments(Model model, @ModelAttribute("message") String message) {
        List<Payment> payments = paymentRepository.findAll();
        model.addAttribute("payments", payments);
        model.addAttribute("message", message);
        return "payment-list";
    }

    // Form thêm / sửa
    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("payment", new Payment());
        return "payment";
    }

    // Edit
    @GetMapping("/edit/{id}")
    public String editPayment(@PathVariable("id") Long id, Model model) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán ID: " + id));
        model.addAttribute("payment", payment);
        return "payment";
    }

    // Lưu (thêm / cập nhật)
    @PostMapping("/save")
    public String savePayment(@ModelAttribute Payment payment, RedirectAttributes redirectAttrs) {
        // Nếu client không gửi paymentDate (form không có trường ngày), gán thời gian hiện tại
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(java.time.LocalDateTime.now());
        }

        paymentRepository.save(payment);
        redirectAttrs.addFlashAttribute("message", "Lưu thanh toán thành công!");
        return "redirect:/payment/list";
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String deletePayment(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        paymentRepository.deleteById(id);
        redirectAttrs.addFlashAttribute("message", "Xóa thanh toán thành công!");
        return "redirect:/payment/list";
    }
}
