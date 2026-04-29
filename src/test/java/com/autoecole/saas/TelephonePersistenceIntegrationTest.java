package com.autoecole.saas;

import com.autoecole.saas.monitor.MonitorService;
import com.autoecole.saas.monitor.dto.CreateMonitorRequest;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.student.StudentService;
import com.autoecole.saas.student.dto.CreateStudentRequest;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.tenant.TenantRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class TelephonePersistenceIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setName("Phone Test Tenant");
        tenant.setStatus("ACTIVE");
        tenant.setContactEmail("phone-test-tenant-" + UUID.randomUUID() + "@test.com");
        tenantRepository.save(tenant);
        TenantContext.setTenantId(tenant.getId());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createStudent_persistsTelephoneOnUserAndStudent() {
        String phone = "0612345678";
        CreateStudentRequest req = new CreateStudentRequest();
        req.setPrenom("Jean");
        req.setNom("Test");
        req.setEmail("student_phone_" + UUID.randomUUID() + "@test.com");
        req.setPassword("secret12");
        req.setLevel("Débutant");
        req.setTelephone(phone);

        Student created = studentService.createStudent(req);
        studentRepository.flush();

        Student reloaded = studentRepository.findById(created.getId()).orElseThrow();
        Assertions.assertEquals(phone, reloaded.getTelephone(), "students.telephone");
        Assertions.assertEquals(phone, reloaded.getUser().getTelephone(), "users.telephone");
    }

    @Test
    void createMonitor_persistsTelephoneOnUserAndMonitor() {
        String phone = "0698765432";
        CreateMonitorRequest req = new CreateMonitorRequest();
        req.setPrenom("Marie");
        req.setNom("Monitrice");
        req.setEmail("monitor_phone_" + UUID.randomUUID() + "@test.com");
        req.setPassword("secret12");
        req.setTelephone(phone);

        var created = monitorService.createMonitor(req);

        Assertions.assertEquals(phone, created.getTelephone(), "monitors.telephone");
        Assertions.assertEquals(phone, created.getUser().getTelephone(), "users.telephone");
    }
}
