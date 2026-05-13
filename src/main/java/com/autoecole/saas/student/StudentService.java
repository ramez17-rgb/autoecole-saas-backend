package com.autoecole.saas.student;

import com.autoecole.saas.monitor.Monitor;
import com.autoecole.saas.monitor.MonitorRepository;
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
    private final MonitorRepository monitorRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public StudentService(
            MonitorRepository monitorRepository,
            UserRepository userRepository,
            StudentRepository studentRepository,
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.monitorRepository = monitorRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Student createStudent(CreateStudentRequest request) {

        UUID tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // 1️⃣ Create USER automatically
        User user = new User();
        user.setFirstName(request.getPrenom());
        user.setLastName(request.getNom());
        user.setEmail(request.getEmail());
        user.setTelephone(request.getTelephone());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Changed from setPasswordHash

        // Handle Role
        RoleEntity role = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(new RoleEntity("STUDENT")));
        user.getRoles().add(role);

        user.setTenant(tenant); // Changed from setTenantId
        user.setStatus("active");

        userRepository.save(user);
        System.out.println("StudentService.createStudent: saved user firstName=" + user.getFirstName() + ", lastName=" + user.getLastName() + ", email=" + user.getEmail());

        Student student = new Student();
        student.setUser(user);
        student.setTenant(tenant); // Changed from setTenantId
        student.setLevel(request.getLevel());
        student.setTelephone(request.getTelephone());
        student.setStatus(StudentStatus.active);

        Student savedStudent = studentRepository.save(student);
        String studentName = user.getFirstName() + " " + user.getLastName();
        eventPublisher.publishEvent(new com.autoecole.saas.audit.AuditLogEvent(this, tenantId, user.getId(), "Élève créé : " + studentName));
        return savedStudent;
    }

    @Transactional
    public Student assignMainMonitor(UUID studentId, UUID monitorId) {

        UUID tenantId = TenantContext.getTenantId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student with ID " + studentId
                        + " not found. Make sure you are using the Student ID, not the User ID."));

        if (student.getTenant() != null && !student.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException(
                    "Student exists but belongs to a different tenant. Cross-tenant access forbidden.");
        }

        Monitor monitor = monitorRepository
                .findById(monitorId)
                .orElseThrow(() -> new RuntimeException("Monitor not found"));

        // tenant safety check
        if (!monitor.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Cross-tenant access forbidden");
        }

        student.setMainMonitor(monitor);
        Student savedStudent = studentRepository.save(student);
        
        String monitorName = monitor.getUser().getFirstName() + " " + monitor.getUser().getLastName();
        String studentName = student.getUser().getFirstName() + " " + student.getUser().getLastName();
        eventPublisher.publishEvent(new com.autoecole.saas.audit.AuditLogEvent(this, tenantId, null, "Moniteur " + monitorName + " assigné à l'élève : " + studentName));
        return savedStudent;
    }

    public List<Student> getAllStudents() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) return studentRepository.findAll();
        return studentRepository.findByTenantId(tenantId);
    }

    public List<Student> getAllStudentsGlobal() {
        return studentRepository.findAll();
    }

    public Student getStudent(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return studentRepository.findById(id)
                .filter(s -> tenantId == null || (s.getTenant() != null && s.getTenant().getId().equals(tenantId)))
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    @Transactional
    public Student registerPublicStudent(String firstName, String lastName, String email, String password, String level, String telephone) {
        // For public registration, we need to associate with a tenant
        // For now, use the first active tenant (this should be improved to allow tenant selection)
        Tenant tenant = tenantRepository.findAll().stream()
                .filter(Tenant::isActive)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active tenant available for registration"));

        // Check if email is already taken
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email is already taken: " + email);
        }

        // 1️⃣ Create USER automatically
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setTelephone(telephone);
        user.setPassword(passwordEncoder.encode(password));
        user.setTenant(tenant);
        user.setStatus("active");

        // Handle Role
        RoleEntity role = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(new RoleEntity("STUDENT")));
        user.getRoles().add(role);

        userRepository.save(user);
        System.out.println("StudentService.registerPublicStudent: saved user firstName=" + user.getFirstName() + ", lastName=" + user.getLastName() + ", email=" + user.getEmail());

        // 2️⃣ Create STUDENT linked to user
        Student student = new Student();
        student.setUser(user);
        student.setTenant(tenant);
        student.setLevel(level);
        student.setStatus(StudentStatus.active);
        student.setTelephone(telephone);

        return studentRepository.save(student);
    }

    public void archiveStudent(UUID id) {
        Student student = getStudent(id);
        student.setStatus(StudentStatus.archived);
        studentRepository.save(student);
    }
}