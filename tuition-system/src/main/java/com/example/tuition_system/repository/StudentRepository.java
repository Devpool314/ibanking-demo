package com.example.tuition_system.repository;

import com.example.tuition_system.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByMssv(String mssv);
}