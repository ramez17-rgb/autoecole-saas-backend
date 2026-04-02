package com.autoecole.saas.user;

import com.autoecole.saas.monitor.Monitor;
import com.autoecole.saas.monitor.MonitorRepository;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final MonitorRepository monitorRepository;

    public ProfileController(UserRepository userRepository, StudentRepository studentRepository, MonitorRepository monitorRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.monitorRepository = monitorRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        User user = getCurrentUser();
        boolean isStudent = user.getRoles().stream().anyMatch(r -> r.getName().equals("STUDENT"));
        boolean isMonitor = user.getRoles().stream().anyMatch(r -> r.getName().equals("MONITOR"));
        boolean isTenantAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("TENANT_ADMIN"));

        Map<String, Object> result = new HashMap<>();
        result.put("email", user.getEmail());

        if (isStudent) {
            Student student = studentRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Student profile not found"));
            result.put("role", "STUDENT");
            result.put("studentId", student.getId());
            result.put("tenantId", student.getTenant().getId());
            result.put("level", student.getLevel() != null ? student.getLevel() : "");
        } else if (isMonitor) {
            Monitor monitor = monitorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Monitor profile not found"));
            result.put("role", "MONITOR");
            result.put("monitorId", monitor.getId());
            result.put("tenantId", monitor.getTenant().getId());
        } else if (isTenantAdmin) {
            result.put("role", "TENANT_ADMIN");
            result.put("tenantId", user.getTenant().getId());
        } else {
            result.put("role", "UNKNOWN");
        }

        return ResponseEntity.ok(result);
    }
}
