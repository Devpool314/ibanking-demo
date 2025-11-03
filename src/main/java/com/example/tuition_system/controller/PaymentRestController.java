package com.example.tuition_system.controller;

import com.example.tuition_system.model.*;
import com.example.tuition_system.model.dto.ApiResponse;
import com.example.tuition_system.model.dto.PaymentCreateDto;
import com.example.tuition_system.model.dto.PaymentVerifyDto;
import com.example.tuition_system.repository.*;
import com.example.tuition_system.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping({"/api/payments", "/api/payment"})
public class PaymentRestController {

    private final PaymentService service;
    private final TuitionInfoRepository tuitionInfoRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PaymentWorkflowService paymentWorkflowService;

    public PaymentRestController(PaymentService service,
                                 TuitionInfoRepository tuitionInfoRepository,
                                 PaymentRepository paymentRepository,
                                 PaymentRequestRepository paymentRequestRepository,
                                 UserRepository userRepository,
                                 OtpService otpService,
                                 PaymentWorkflowService paymentWorkflowService) {
        this.service = service;
        this.tuitionInfoRepository = tuitionInfoRepository;
        this.paymentRepository = paymentRepository;
        this.paymentRequestRepository = paymentRequestRepository;
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.paymentWorkflowService = paymentWorkflowService;
    }

    // Existing payments API
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

        System.out.println("🔹 Nhận yêu cầu thanh toán cho MSSV: " + mssv);

        Optional<TuitionInfo> infoOpt = tuitionInfoRepository.findByMssv(mssv);
        if (infoOpt.isPresent()) {
            TuitionInfo info = infoOpt.get();
            info.setPaid(true);
            tuitionInfoRepository.save(info);
            System.out.println("✅ Đã cập nhật học phí cho sinh viên: " + info.getFullname());
        } else {
            System.out.println("⚠️ Không tìm thấy sinh viên có MSSV: " + mssv);
        }

        return service.process(p);
    }

    @GetMapping("/check/{mssv}")
    public Map<String, Object> getTuitionByMssv(@PathVariable String mssv) {
        Map<String, Object> response = new HashMap<>();

        Optional<TuitionInfo> optionalInfo = tuitionInfoRepository.findByMssv(mssv);

        if (optionalInfo.isPresent()) {
            TuitionInfo info = optionalInfo.get();
            response.put("found", true);
            response.put("fullname", info.getFullname());
            response.put("mssv", info.getMssv());
            response.put("amount", info.getAmount());
            response.put("paid", info.isPaid());
        } else {
            response.put("found", false);
            response.put("message", "Không tìm thấy thông tin học phí cho MSSV: " + mssv);
        }

        return response;
    }

    @PostMapping("/updateStatus/{mssv}")
    public Map<String, Object> updateTuitionStatus(@PathVariable String mssv) {
        Map<String, Object> result = new HashMap<>();
        Optional<TuitionInfo> infoOpt = tuitionInfoRepository.findByMssv(mssv);

        if (infoOpt.isPresent()) {
            TuitionInfo info = infoOpt.get();
            info.setPaid(true);
            tuitionInfoRepository.save(info);
            result.put("success", true);
            result.put("message", "Cập nhật trạng thái học phí thành công.");
        } else {
            result.put("success", false);
            result.put("message", "Không tìm thấy sinh viên có MSSV: " + mssv);
        }

        return result;
    }

    // Compatibility endpoints used by frontend (from the old PaymentController)
    @GetMapping("/list")
    public List<Payment> listPayments() {
        return paymentRepository.findAll();
    }

    @PostMapping("/request")
    public ResponseEntity<?> createPaymentRequest(@RequestBody PaymentCreateDto paymentDto,
                                                  Principal principal) {
        if (!paymentDto.termsAccepted()) {
            return ResponseEntity.badRequest().body(new ApiResponse("Vui lòng đồng ý điều khoản trước khi thanh toán!"));
        }

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

        String devOtp = otpService.sendOtp(pr.getId(), payerEmail);

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

        return ResponseEntity.ok(new ApiResponse("Thanh toán thành công!"));
    }

}
