package com.autoecole.saas.student;

import com.autoecole.saas.student.dto.CreateStudentRequest;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.tenant.TenantRepository;
import com.autoecole.saas.user.RoleEntity;
import com.autoecole.saas.user.RoleRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Student createStudent(CreateStudentRequest request) {

        UUID tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // 1️⃣ Create USER automatically
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Changed from setPasswordHash

        // Handle Role
        RoleEntity role = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(new RoleEntity("STUDENT")));
        user.getRoles().add(role);

        user.setTenant(tenant); // Changed from setTenantId
        user.setStatus("active");

        userRepository.save(user);

        // 2️⃣ Create STUDENT linked to user
        Student student = new Student();
        student.setUser(user);
        student.setTenant(tenant); // Changed from setTenantId
        student.setLevel(request.getLevel());
        student.setStatus(StudentStatus.active);

        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findByTenantId(TenantContext.getTenantId());
    }

    public Student getStudent(UUID id) {
        return studentRepository.findById(id)
                .filter(s -> s.getTenant().getId().equals(TenantContext.getTenantId()))
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public void archiveStudent(UUID id) {
        Student student = getStudent(id);
        student.setStatus(StudentStatus.archived);
        studentRepository.save(student);
    }
}
