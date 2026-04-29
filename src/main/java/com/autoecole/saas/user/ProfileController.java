package com.autoecole.saas.user;

import com.autoecole.saas.monitor.MonitorRepository;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.TenantContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final StudentRepository studentRepository;
    private final MonitorRepository monitorRepository;

    public ProfileController(StudentRepository studentRepository, MonitorRepository monitorRepository) {
        this.studentRepository = studentRepository;
        this.monitorRepository = monitorRepository;
    }

    @GetMapping("/me")
    public Map<String, Object> getMyProfile(@AuthenticationPrincipal User user) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getId());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("telephone", user.getTelephone());

        // Determine role(s)
        String role = user.getRoles().stream()
                .map(r -> r.getName())
                .findFirst()
                .orElse("UNKNOWN");
        profile.put("role", role);

        UUID tenantId = TenantContext.getTenantId();

        // If user is a student, find their studentId
        if ("STUDENT".equals(role) || "ELEVE".equals(role)) {
            if (tenantId != null) {
                studentRepository.findByTenantId(tenantId).stream()
                        .filter(s -> s.getUser() != null && s.getUser().getId().equals(user.getId()))
                        .findFirst()
                        .ifPresent(s -> profile.put("studentId", s.getId()));
            } else {
                // Fallback: find student by user across all tenants
                studentRepository.findAll().stream()
                        .filter(s -> s.getUser() != null && s.getUser().getId().equals(user.getId()))
                        .findFirst()
                        .ifPresent(s -> profile.put("studentId", s.getId()));
            }
        }

        // If user is a monitor, find their monitorId
        if ("MONITOR".equals(role) || "MONITEUR".equals(role)) {
            if (tenantId != null) {
                monitorRepository.findByTenantId(tenantId).stream()
                        .filter(m -> m.getUser().getId().equals(user.getId()))
                        .findFirst()
                        .ifPresent(m -> profile.put("monitorId", m.getId()));
            } else {
                monitorRepository.findAll().stream()
                        .filter(m -> m.getUser().getId().equals(user.getId()))
                        .findFirst()
                        .ifPresent(m -> profile.put("monitorId", m.getId()));
            }
        }

        return profile;
    }
}
