package com.autoecole.saas.mock;

import org.springframework.web.bind.annotation.*;
import com.autoecole.saas.student.StudentService;
import com.autoecole.saas.monitor.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;

@RestController
public class MigrationController {
    @Autowired private StudentService studentService;
    @Autowired private com.autoecole.saas.student.StudentRepository studentRepository;
    
    @GetMapping("/api/auth/debug-students")
    public Object fetch() {
        return studentRepository.findAll();
    }
}
