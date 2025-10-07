package com.example.tuition_system.repository;

import com.example.tuition_system.model.TuitionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TuitionInfoRepository extends JpaRepository<TuitionInfo, Long> {
    Optional<TuitionInfo> findByMssv(String mssv);
}
