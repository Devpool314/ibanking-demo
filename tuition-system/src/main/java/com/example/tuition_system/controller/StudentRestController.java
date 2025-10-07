package com.example.tuition_system.controller;

import com.example.tuition_system.model.Student;
import com.example.tuition_system.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentRestController {

    private final StudentRepository repository;

    public StudentRestController(StudentRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    @PostMapping
    public Student addStudent(@RequestParam String mssv,
                              @RequestParam String name,
                              @RequestParam double tuitionFee) {
        Student s = new Student(mssv, name, tuitionFee);
        return repository.save(s);
    }

    @GetMapping("/{mssv}")
    public Student getByMssv(@PathVariable String mssv) {
        return repository.findByMssv(mssv).orElse(null);
    }
}
