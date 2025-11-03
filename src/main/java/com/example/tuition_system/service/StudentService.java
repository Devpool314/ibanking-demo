package com.example.tuition_system.service;

import com.example.tuition_system.model.Student;
import com.example.tuition_system.model.TuitionInfo;
import com.example.tuition_system.model.dto.StudentDto;
import com.example.tuition_system.repository.StudentRepository;
import com.example.tuition_system.repository.TuitionInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final TuitionInfoRepository tuitionInfoRepository;

    public StudentService(StudentRepository studentRepository, 
                          TuitionInfoRepository tuitionInfoRepository) {
        this.studentRepository = studentRepository;
        this.tuitionInfoRepository = tuitionInfoRepository;
    }

    @Transactional
    public Student createStudent(StudentDto studentDto) {

        Student s = new Student(
            studentDto.mssv(), 
            studentDto.name(), 
            studentDto.tuitionFee()
        );
        Student savedStudent = studentRepository.save(s);

        TuitionInfo tuition = new TuitionInfo();
        tuition.setMssv(studentDto.mssv());
        tuition.setAmount(studentDto.tuitionFee());
        tuition.setPaid(false); 
        tuition.setFullname(studentDto.name()); 
        
        tuitionInfoRepository.save(tuition);

        return savedStudent;
    }
}