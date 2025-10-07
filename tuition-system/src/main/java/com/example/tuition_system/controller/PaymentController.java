package com.example.tuition_system.controller;

import com.example.tuition_system.model.Payment;
import com.example.tuition_system.model.TuitionInfo;
import com.example.tuition_system.repository.PaymentRepository;
import com.example.tuition_system.repository.TuitionInfoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final TuitionInfoRepository tuitionInfoRepository;

    public PaymentController(PaymentRepository paymentRepository,
                             TuitionInfoRepository tuitionInfoRepository) {
        this.paymentRepository = paymentRepository;
        this.tuitionInfoRepository = tuitionInfoRepository;
    }

    // Hiển thị danh sách thanh toán
    @GetMapping("/list")
    public String listPayments(Model model, @ModelAttribute("message") String message) {
        List<Payment> payments = paymentRepository.findAll();
        model.addAttribute("payments", payments);
        model.addAttribute("message", message);

        // Lấy tên người đang đăng nhập để hiển thị
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            model.addAttribute("currentUser", auth.getName());
        }

        return "payment-list";
    }

    // Hiển thị form thêm/sửa thanh toán
    @GetMapping("/form")
    public String showForm(Model model) {
        Payment payment = new Payment();

        // Lấy người dùng hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String username = auth.getName();
            payment.setPayerUsername(username); // Gán sẵn vào payment object
            model.addAttribute("currentUser", username);
        }

        model.addAttribute("payment", payment);
        return "payment";
    }

    // Sửa thanh toán
    @GetMapping("/edit/{id}")
    public String editPayment(@PathVariable("id") Long id, Model model) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán ID: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            model.addAttribute("currentUser", auth.getName());
        }

        model.addAttribute("payment", payment);
        return "payment";
    }

    // ✅ Lưu thanh toán và cập nhật trạng thái học phí
    @PostMapping("/save")
    public String savePayment(@ModelAttribute Payment payment, RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "unknown";

        payment.setPayerUsername(username);

        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        // ✅ Lưu vào bảng payment
        paymentRepository.save(payment);

        // ✅ Cập nhật trạng thái học phí của sinh viên
        try {
            TuitionInfo info = tuitionInfoRepository.findByMssv(payment.getMssv())
                    .orElse(null);
            if (info != null) {
                info.setPaid(true);
                tuitionInfoRepository.save(info);
                System.out.println("✅ Đã cập nhật học phí cho sinh viên: " + info.getFullname() + " (MSSV: " + info.getMssv() + ")");
            } else {
                System.out.println("⚠️ Không tìm thấy sinh viên có MSSV: " + payment.getMssv());
            }
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi cập nhật học phí: " + e.getMessage());
        }

        redirectAttrs.addFlashAttribute("message", "Lưu thanh toán thành công!");
        return "redirect:/payment/list";
    }

    // Xóa thanh toán
    @GetMapping("/delete/{id}")
    public String deletePayment(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        paymentRepository.deleteById(id);
        redirectAttrs.addFlashAttribute("message", "Xóa thanh toán thành công!");
        return "redirect:/payment/list";
    }
}
