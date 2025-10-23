package com.example.tuition_system.controller;

import com.example.tuition_system.model.Payment;
import com.example.tuition_system.model.TuitionInfo;
import com.example.tuition_system.repository.TuitionInfoRepository;
import com.example.tuition_system.service.PaymentService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {

    private final PaymentService service;
    private final TuitionInfoRepository tuitionInfoRepository;

    public PaymentRestController(PaymentService service, TuitionInfoRepository tuitionInfoRepository) {
        this.service = service;
        this.tuitionInfoRepository = tuitionInfoRepository;
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
    

}