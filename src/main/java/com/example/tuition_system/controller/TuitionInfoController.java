package com.example.tuition_system.controller;

import com.example.tuition_system.model.TuitionInfo;
import com.example.tuition_system.repository.TuitionInfoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tuition")
public class TuitionInfoController {

    private final TuitionInfoRepository tuitionInfoRepository;

    public TuitionInfoController(TuitionInfoRepository tuitionInfoRepository) {
        this.tuitionInfoRepository = tuitionInfoRepository;
    }

    @GetMapping("/{mssv}")
    public ResponseEntity<TuitionInfo> getTuitionInfoByMssv(@PathVariable String mssv) {
        return tuitionInfoRepository.findByMssv(mssv)
                .map(tuitionInfo -> ResponseEntity.ok(tuitionInfo))
                .orElse(ResponseEntity.notFound().build()); 
    }
}