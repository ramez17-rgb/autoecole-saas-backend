package com.autoecole.saas.controller;

import com.autoecole.saas.service.SuperAdminService;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.monitor.Monitor;
import com.autoecole.saas.monitor.MonitorRepository;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final TenantRepository tenantRepository;
    private final StudentRepository studentRepository;
    private final MonitorRepository monitorRepository;

    public SuperAdminController(SuperAdminService superAdminService, TenantRepository tenantRepository, StudentRepository studentRepository, MonitorRepository monitorRepository) {
        this.superAdminService = superAdminService;
        this.tenantRepository = tenantRepository;
        this.studentRepository = studentRepository;
        this.monitorRepository = monitorRepository;
    }

    @GetMapping("/tenants")
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @GetMapping("/monitors")
    public List<Monitor> getAllMonitors() {
        return monitorRepository.findAll();
    }

    @PutMapping("/validate/{tenantId}")
    public ResponseEntity<String> validateTenant(@PathVariable UUID tenantId) {
        String token = superAdminService.validateTenant(tenantId);
        return ResponseEntity.ok("Tenant and Admin validated successfully. Setup Token: " + token);
    }

    @PutMapping("/reject/{tenantId}")
    public ResponseEntity<String> rejectTenant(@PathVariable UUID tenantId,
            @RequestBody(required = false) String reason) {
        superAdminService.rejectTenant(tenantId, reason != null ? reason : "No reason provided.");
        return ResponseEntity.ok("Tenant rejected.");
    }
}
