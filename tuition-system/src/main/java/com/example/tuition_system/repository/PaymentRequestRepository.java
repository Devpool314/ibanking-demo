package com.example.tuition_system.repository;

import com.example.tuition_system.model.PaymentRequest;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pr from PaymentRequest pr where pr.id = :id")
    Optional<PaymentRequest> lockById(@Param("id") Long id);
}
