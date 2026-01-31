package com.autoecole.saas.student;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public Student createStudent(
            @RequestParam UUID userId,
            @RequestParam String level
    ) {
        return studentService.createStudent(userId, level);
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/{id}")
    public Student getStudent(@PathVariable UUID id) {
        return studentService.getStudent(id);
    }

    @DeleteMapping("/{id}")
    public void archiveStudent(@PathVariable UUID id) {
        studentService.archiveStudent(id);
    }
}
