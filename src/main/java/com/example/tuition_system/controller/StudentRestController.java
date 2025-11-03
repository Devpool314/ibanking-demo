package com.example.tuition_system.controller;

import com.example.tuition_system.model.Student;
import com.example.tuition_system.model.dto.StudentDto;
import com.example.tuition_system.repository.StudentRepository;
import com.example.tuition_system.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentRestController {

    private final StudentService studentService;
    private final StudentRepository repository;

    public StudentRestController(StudentService studentService, StudentRepository repository) {
        this.studentService = studentService;
        this.repository = repository;
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Student addStudent(@RequestBody StudentDto studentDto) {
        return studentService.createStudent(studentDto); 
    }

    @GetMapping("/{mssv}")
    public ResponseEntity<Student> getByMssv(@PathVariable String mssv) {
        return repository.findByMssv(mssv)
                .map(student -> ResponseEntity.ok(student))
                .orElse(ResponseEntity.notFound().build());
    }
}