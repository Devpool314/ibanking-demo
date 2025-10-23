package com.example.tuition_system.repository;

import com.example.tuition_system.model.TuitionInfo;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TuitionInfoRepository extends JpaRepository<TuitionInfo, Long> {
    Optional<TuitionInfo> findByMssv(String mssv);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TuitionInfo t where t.mssv = :mssv")
    Optional<TuitionInfo> lockByMssv(@Param("mssv") String mssv);

}
