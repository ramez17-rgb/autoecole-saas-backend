package com.autoecole.saas.student;

import com.autoecole.saas.monitor.Monitor;
import com.autoecole.saas.monitor.MonitorRepository;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.tenant.TenantRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@SpringBootTest
@Transactional
public class StudentAssignmentIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        // 1. Create a Tenant
        tenant = new Tenant();
        tenant.setName("Integration Test Tenant");
        tenant.setStatus("active");
        tenantRepository.save(tenant);

        // 2. Set TenantContext
        TenantContext.setTenantId(tenant.getId());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testAssignMainMonitor() {
        // 3. Create Monitor User & Entity
        User monitorUser = new User();
        monitorUser.setEmail("monitor_" + UUID.randomUUID() + "@test.com");
        monitorUser.setPassword(passwordEncoder.encode("password"));
        monitorUser.setTenant(tenant);
        monitorUser.setStatus("active");
        userRepository.save(monitorUser);

        Monitor monitor = new Monitor();
        monitor.setUser(monitorUser);
        monitor.setTenant(tenant);
        monitorRepository.save(monitor);

        // 4. Create Student User & Entity
        User studentUser = new User();
        studentUser.setEmail("student_" + UUID.randomUUID() + "@test.com");
        studentUser.setPassword(passwordEncoder.encode("password"));
        studentUser.setTenant(tenant);
        studentUser.setStatus("active");
        userRepository.save(studentUser);

        Student student = new Student();
        student.setUser(studentUser);
        student.setTenant(tenant);
        student.setStatus(StudentStatus.active);
        studentRepository.save(student);

        // 5. Execute Assignment
        System.out.println("Assigning Monitor ID: " + monitor.getId() + " to Student ID: " + student.getId());
        studentService.assignMainMonitor(student.getId(), monitor.getId());

        // 6. Verify
        Student updatedStudent = studentRepository.findById(student.getId()).orElseThrow();
        Assertions.assertNotNull(updatedStudent.getMainMonitor(), "Main Monitor should not be null");
        Assertions.assertEquals(monitor.getId(), updatedStudent.getMainMonitor().getId(), "Monitor ID should match");
        System.out.println("Verification Successful: Student " + updatedStudent.getId() + " is assigned to Monitor "
                + updatedStudent.getMainMonitor().getId());
    }
}
