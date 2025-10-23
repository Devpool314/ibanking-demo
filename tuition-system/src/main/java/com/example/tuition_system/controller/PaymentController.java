package com.example.tuition_system.controller;

import com.example.tuition_system.model.*;
import com.example.tuition_system.repository.*;
import com.example.tuition_system.service.OtpService;
import com.example.tuition_system.service.PaymentWorkflowService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final TuitionInfoRepository tuitionInfoRepository;
    private final UserRepository userRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final OtpService otpService;
    private final PaymentWorkflowService paymentWorkflowService;

    public PaymentController(PaymentRepository paymentRepository,
                             TuitionInfoRepository tuitionInfoRepository,
                             UserRepository userRepository,
                             PaymentRequestRepository paymentRequestRepository,
                             OtpService otpService,
                             PaymentWorkflowService paymentWorkflowService) {
        this.paymentRepository = paymentRepository;
        this.tuitionInfoRepository = tuitionInfoRepository;
        this.userRepository = userRepository;
        this.paymentRequestRepository = paymentRequestRepository;
        this.otpService = otpService;
        this.paymentWorkflowService = paymentWorkflowService;
    }

    @GetMapping("/list")
    public String listPayments(Model model, @RequestParam(value = "message", required = false) String message) {
        List<Payment> payments = paymentRepository.findAll();
        model.addAttribute("payments", payments);
        if (message != null) model.addAttribute("message", message);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) model.addAttribute("currentUser", auth.getName());
        return "payment-list";
    }

    // GET /payment/request: chỉ trả text để tránh 500 khi gõ nhầm
    @GetMapping("/request")
    @ResponseBody
    public String requestGetDebug() {
        return "GET /payment/request OK — hãy mở /payment/form và Submit để POST /payment/request.";
    }

    @GetMapping("/form")
    public String showForm(Model model, Principal principal) {
        Payment payment = new Payment();
        if (principal != null) {
            payment.setPayerUsername(principal.getName());
            model.addAttribute("currentUser", principal.getName());
        } else {
            model.addAttribute("currentUser", "Khách");
        }
        double availableBalance = 0.0;
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByUsername(principal.getName());
            if (optionalUser.isPresent()) availableBalance = optionalUser.get().getBalance();
        }
        payment.setAvailableBalance(availableBalance);
        model.addAttribute("payment", payment);
        model.addAttribute("availableBalance", availableBalance);
        return "payment";
    }

    // ===== Bước 1: tạo yêu cầu + gửi OTP tới email người nộp
    @PostMapping("/request")
    public String createPaymentRequest(@ModelAttribute Payment payment,
                                       Principal principal,
                                       RedirectAttributes ra) {
        if (!payment.isTermsAccepted()) {
            ra.addFlashAttribute("message", "Vui lòng đồng ý điều khoản trước khi thanh toán!");
            return "redirect:/payment/form";
        }

        if (principal == null) {
            ra.addFlashAttribute("message", "Bạn chưa đăng nhập!");
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("message", "Không tìm thấy người dùng!");
            return "redirect:/payment/form";
        }
        String payerEmail = user.getEmail();
        if (payerEmail == null || payerEmail.isBlank()) {
            ra.addFlashAttribute("message", "Tài khoản của bạn chưa có email. Vui lòng cập nhật email trước khi tạo giao dịch.");
            return "redirect:/payment/form";
        }

        TuitionInfo tuition = tuitionInfoRepository.findByMssv(payment.getMssv()).orElse(null);
        if (tuition == null) {
            ra.addFlashAttribute("message", "Không tìm thấy thông tin học phí!");
            return "redirect:/payment/form";
        }
        if (tuition.isPaid()) {
            ra.addFlashAttribute("message", "Khoản học phí này đã được thanh toán.");
            return "redirect:/payment/form";
        }

        if (Double.compare(payment.getAmount(), tuition.getAmount()) != 0) {
            ra.addFlashAttribute("message", "Số tiền phải đúng bằng học phí: " + tuition.getAmount());
            return "redirect:/payment/form";
        }
        if (user.getBalance() < payment.getAmount()) {
            ra.addFlashAttribute("message", "Số dư không đủ để tạo giao dịch!");
            return "redirect:/payment/form";
        }

        PaymentRequest pr = new PaymentRequest();
        pr.setMssv(payment.getMssv());
        pr.setAmount(payment.getAmount());
        pr.setPayerUsername(username);
        pr.setPayerEmail(payerEmail);                  // <<< lưu email người nộp
        pr.setStatus(PaymentRequest.Status.PENDING);
        pr.setCreatedAt(LocalDateTime.now());
        pr.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        paymentRequestRepository.save(pr);             // sinh transactionId

        otpService.sendOtp(pr.getId(), payerEmail);    // gửi OTP tới email người nộp

        String otp = otpService.sendOtp(pr.getId(), user.getEmail());
        ra.addFlashAttribute("devOtp", otp);   // <— THÊM DÒNG NÀY

        return "redirect:/payment/verify?transactionId=" + pr.getId();
    }

    @GetMapping("/verify")
    public String verifyPage(@RequestParam Long transactionId, Model model) {
        model.addAttribute("transactionId", transactionId);
        return "transaction-otp";
    }

    // ===== Bước 2: xác thực OTP -> xử lý thanh toán và gửi mail xác nhận
    @PostMapping("/verify")
    public String verify(@RequestParam Long transactionId,
                         @RequestParam String otp,
                         Principal principal,
                         RedirectAttributes ra) {

        PaymentRequest pr = paymentRequestRepository.findById(transactionId).orElse(null);
        if (pr == null) {
            ra.addFlashAttribute("message", "Yêu cầu không tồn tại.");
            return "redirect:/payment/form";
        }

        if (LocalDateTime.now().isAfter(pr.getExpiresAt())) {
            pr.setStatus(PaymentRequest.Status.EXPIRED);
            paymentRequestRepository.save(pr);
            ra.addFlashAttribute("message", "OTP đã hết hạn. Vui lòng tạo yêu cầu mới.");
            return "redirect:/payment/form";
        }

        if (!otpService.verify(transactionId, otp)) {
            ra.addFlashAttribute("message", "OTP không hợp lệ.");
            return "redirect:/payment/verify?transactionId=" + transactionId;
        }

        pr.setStatus(PaymentRequest.Status.CONFIRMED);
        paymentRequestRepository.save(pr);

        // Xử lý giao dịch + gửi mail xác nhận trong service
        paymentWorkflowService.processConfirmed(transactionId, principal.getName());

        ra.addFlashAttribute("message", "Thanh toán thành công!");
        return "redirect:/payment/list";
    }
}
