package com.autoecole.saas.student;

import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public StudentService(
            StudentRepository studentRepository,
            UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    public Student createStudent(UUID userId, String level) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = new Student();
        student.setUser(user);
        student.setTenant(user.getTenant());
        student.setLevel(level);

        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudent(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public void archiveStudent(UUID id) {
        Student student = getStudent(id);
        student.setStatus(StudentStatus.archived);
        studentRepository.save(student);
    }
}
