package com.example.tuition_system.controller;

import com.example.tuition_system.model.*;
import com.example.tuition_system.model.dto.ApiResponse; // <-- Import DTO
import com.example.tuition_system.model.dto.PaymentCreateDto; // <-- Import DTO
import com.example.tuition_system.model.dto.PaymentVerifyDto; // <-- Import DTO
import com.example.tuition_system.repository.*;
import com.example.tuition_system.service.OtpService;
import com.example.tuition_system.service.PaymentWorkflowService;
import org.springframework.http.ResponseEntity; // <-- Dùng ResponseEntity
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // <-- Dùng để lấy user đã đăng nhập
import java.time.LocalDateTime;
import java.util.List;

// 1. THAY ĐỔI: @RestController
@RestController
// 2. THAY ĐỔI: Thêm /api/ vào đường dẫn
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final TuitionInfoRepository tuitionInfoRepository;
    private final UserRepository userRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final OtpService otpService;
    private final PaymentWorkflowService paymentWorkflowService;

    // Constructor giữ nguyên
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

    // 3. THAY ĐỔI: Hàm listPayments
    // Bỏ Model, @RequestParam, trả về List<Payment> trực tiếp
    @GetMapping("/list")
    public List<Payment> listPayments() {
        return paymentRepository.findAll();
    }

    // 5. THAY ĐỔI LỚN: Hàm createPaymentRequest
    @PostMapping("/request")
    public ResponseEntity<?> createPaymentRequest(@RequestBody PaymentCreateDto paymentDto,
                                                   Principal principal) {
        if (!paymentDto.termsAccepted()) {
            // Thay vì Redirect, trả về lỗi 400 Bad Request
            return ResponseEntity.badRequest().body(new ApiResponse("Vui lòng đồng ý điều khoản trước khi thanh toán!"));
        }

        // Principal là cách chuẩn để lấy user đã đăng nhập (bằng JWT)
        if (principal == null) {
            return ResponseEntity.status(401).body(new ApiResponse("Bạn chưa đăng nhập!"));
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(new ApiResponse("Không tìm thấy người dùng!"));
        }
        String payerEmail = user.getEmail();
        if (payerEmail == null || payerEmail.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse("Tài khoản của bạn chưa có email. Vui lòng cập nhật email trước khi tạo giao dịch."));
        }

        TuitionInfo tuition = tuitionInfoRepository.findByMssv(paymentDto.mssv()).orElse(null);
        if (tuition == null) {
            return ResponseEntity.status(404).body(new ApiResponse("Không tìm thấy thông tin học phí!"));
        }
        if (tuition.isPaid()) {
            return ResponseEntity.badRequest().body(new ApiResponse("Khoản học phí này đã được thanh toán."));
        }

        if (Double.compare(paymentDto.amount(), tuition.getAmount()) != 0) {
            return ResponseEntity.badRequest().body(new ApiResponse("Số tiền phải đúng bằng học phí: " + tuition.getAmount()));
        }
        if (user.getBalance() < paymentDto.amount()) {
            return ResponseEntity.badRequest().body(new ApiResponse("Số dư không đủ để tạo giao dịch!"));
        }

        PaymentRequest pr = new PaymentRequest();
        pr.setMssv(paymentDto.mssv());
        pr.setAmount(paymentDto.amount());
        pr.setPayerUsername(username);
        pr.setPayerEmail(payerEmail);
        pr.setStatus(PaymentRequest.Status.PENDING);
        pr.setCreatedAt(LocalDateTime.now());
        pr.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        paymentRequestRepository.save(pr);

        // Gửi OTP VÀ HỨNG LẠI MÃ OTP
        String devOtp = otpService.sendOtp(pr.getId(), payerEmail);

        // 6. THAY ĐỔI: Trả về ID giao dịch VÀ OTP (để test)
        var responseData = new Object() {
            public Long transactionId = pr.getId();
            public String otp = devOtp;
        };

        return ResponseEntity.ok(new ApiResponse(
            "Yêu cầu thanh toán đã được tạo, vui lòng xác thực OTP.", 
            responseData 
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody PaymentVerifyDto verifyDto,
                                     Principal principal) {

        PaymentRequest pr = paymentRequestRepository.findById(verifyDto.transactionId()).orElse(null);
        if (pr == null) {
            return ResponseEntity.status(404).body(new ApiResponse("Yêu cầu không tồn tại."));
        }

        if (LocalDateTime.now().isAfter(pr.getExpiresAt())) {
            pr.setStatus(PaymentRequest.Status.EXPIRED);
            paymentRequestRepository.save(pr);
            return ResponseEntity.badRequest().body(new ApiResponse("OTP đã hết hạn. Vui lòng tạo yêu cầu mới."));
        }

        if (!otpService.verify(verifyDto.transactionId(), verifyDto.otp())) {
            return ResponseEntity.badRequest().body(new ApiResponse("OTP không hợp lệ."));
        }

        pr.setStatus(PaymentRequest.Status.CONFIRMED);
        paymentRequestRepository.save(pr);

        paymentWorkflowService.processConfirmed(verifyDto.transactionId(), principal.getName());

        // 9. THAY ĐỔI: Trả về thông báo thành công
        return ResponseEntity.ok(new ApiResponse("Thanh toán thành công!"));
    }
}