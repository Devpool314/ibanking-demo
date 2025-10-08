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
import java.util.Optional;

import com.example.tuition_system.model.User;
import com.example.tuition_system.repository.UserRepository;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final TuitionInfoRepository tuitionInfoRepository;
    private final UserRepository userRepository; // ✅ thêm dòng này

    // ✅ cập nhật constructor để nhận thêm UserRepository
    public PaymentController(PaymentRepository paymentRepository,
                             TuitionInfoRepository tuitionInfoRepository,
                             UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.tuitionInfoRepository = tuitionInfoRepository;
        this.userRepository = userRepository;
    }

    // Hiển thị danh sách thanh toán
    @GetMapping("/list")
    public String listPayments(Model model, 
                            @RequestParam(value = "message", required = false) String message) {
        List<Payment> payments = paymentRepository.findAll();
        model.addAttribute("payments", payments);
        if (message != null) {
            model.addAttribute("message", message);
        }

        // Lấy tên người đang đăng nhập để hiển thị
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            model.addAttribute("currentUser", auth.getName());
        }

    return "payment-list";
}


    // Hiển thị form thêm/sửa thanh toán
    @GetMapping("/form")
public String showForm(Model model, Principal principal) {
    Payment payment = new Payment();

    // ✅ Lấy tên người dùng hiện tại (người nộp tiền)
    if (principal != null) {
        payment.setPayerUsername(principal.getName());
        model.addAttribute("currentUser", principal.getName());
    } else {
        model.addAttribute("currentUser", "Khách");
    }

    // ✅ Lấy số dư khả dụng của người dùng hiện tại
    double availableBalance = 0.0;
    if (principal != null) {
        // Ví dụ nếu bạn lưu thông tin số dư trong bảng users:
        // (Giả sử có UserRepository)
        Optional<User> optionalUser = userRepository.findByUsername(principal.getName());
        if (optionalUser.isPresent()) {
            availableBalance = optionalUser.get().getBalance();
        }
    }

    // ✅ Gán số dư khả dụng vào Payment để hiển thị lên form
    payment.setAvailableBalance(availableBalance);

    // ✅ Thêm các attribute vào model (hiển thị trong form)
    model.addAttribute("payment", payment);
    model.addAttribute("availableBalance", availableBalance);

    return "payment"; // trỏ đến file payment.html
}


    // Sửa thanh toán
    @PostMapping("/save")
public String savePayment(@ModelAttribute Payment payment,
                          RedirectAttributes redirectAttributes) {

    if (!payment.isTermsAccepted()) {
        redirectAttributes.addFlashAttribute("message", "Vui lòng đồng ý với các điều khoản trước khi thanh toán!");
        return "redirect:/payment/form";    
    }


    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();

    

    // Lấy người dùng hiện tại
    User user = userRepository.findByUsername(username).orElse(null);
    if (user == null) {
        redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng!");
        return "redirect:/payment/form";
    }

    // Lấy thông tin học phí của sinh viên tương ứng
    TuitionInfo tuition = tuitionInfoRepository.findByMssv(payment.getMssv()).orElse(null);
    if (tuition == null) {
        redirectAttributes.addFlashAttribute("message", "Không tìm thấy thông tin học phí!");
        return "redirect:/payment/form";
    }

    // Nếu số dư khả dụng nhỏ hơn số tiền thanh toán
    if (user.getBalance() < payment.getAmount()) {
        redirectAttributes.addFlashAttribute("message", "Số dư không đủ để thanh toán!");
        return "redirect:/payment/form";
    }

    // Trừ tiền trong tài khoản
    double newBalance = user.getBalance() - payment.getAmount();
    user.setBalance(newBalance);
    userRepository.save(user);

    // Đánh dấu học phí là đã đóng nếu số tiền >= học phí
    if (payment.getAmount() >= tuition.getAmount()) {
        tuition.setPaid(true);
        tuitionInfoRepository.save(tuition);
    }

    // Lưu thông tin thanh toán
    payment.setPaymentDate(java.time.LocalDateTime.now());
    payment.setPayerUsername(username);
    payment.setAvailableBalance(newBalance);
    payment.setPaid(true); // thanh toán đã hoàn tất
    paymentRepository.save(payment);

    redirectAttributes.addFlashAttribute("message", "Thanh toán thành công!");
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