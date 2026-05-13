package com.autoecole.saas.user;

import com.autoecole.saas.joinrequest.JoinRequestStatus;
import com.autoecole.saas.joinrequest.SchoolJoinRequest;
import com.autoecole.saas.joinrequest.SchoolJoinRequestRepository;
import com.autoecole.saas.monitor.MonitorRepository;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final StudentRepository studentRepository;
    private final MonitorRepository monitorRepository;
    private final SchoolJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(StudentRepository studentRepository,
                             MonitorRepository monitorRepository,
                             SchoolJoinRequestRepository joinRequestRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.monitorRepository = monitorRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        // If user is a student, find their studentId and tenant info
        if ("STUDENT".equals(role) || "ELEVE".equals(role)) {
            Optional<Student> studentOpt;
            if (tenantId != null) {
                studentOpt = studentRepository.findByTenantId(tenantId).stream()
                        .filter(s -> s.getUser() != null && s.getUser().getId().equals(user.getId()))
                        .findFirst();
            } else {
                // Fallback: find student by user across all tenants
                studentOpt = studentRepository.findByUserId(user.getId());
            }
            studentOpt.ifPresent(s -> {
                profile.put("studentId", s.getId());
                // Expose tenantId so frontend knows if student is assigned to a school
                if (s.getTenant() != null) {
                    profile.put("tenantId", s.getTenant().getId());
                    profile.put("schoolName", s.getTenant().getName());
                } else {
                    // No tenant assigned — check for pending/rejected join request
                    List<SchoolJoinRequest> requests = joinRequestRepository.findByStudentId(s.getId());
                    boolean hasPending = requests.stream()
                            .anyMatch(r -> r.getStatus() == JoinRequestStatus.PENDING);
                    if (hasPending) {
                        profile.put("joinRequestStatus", "PENDING");
                    } else {
                        boolean hasRejected = requests.stream()
                                .anyMatch(r -> r.getStatus() == JoinRequestStatus.REJECTED);
                        if (hasRejected) {
                            profile.put("joinRequestStatus", "REJECTED");
                        }
                    }
                }
            });
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

    /**
     * Update the current user's profile (name, email, telephone).
     * Optionally change password if currentPassword + newPassword are provided.
     */
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest request) {

        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.firstName() != null && !request.firstName().isBlank()) {
            dbUser.setFirstName(request.firstName());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            dbUser.setLastName(request.lastName());
        }
        if (request.telephone() != null) {
            dbUser.setTelephone(request.telephone());
        }
        // Email update (check uniqueness if changed)
        if (request.email() != null && !request.email().isBlank() && !request.email().equals(dbUser.getEmail())) {
            if (userRepository.findByEmail(request.email()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cet email est déjà utilisé."));
            }
            dbUser.setEmail(request.email());
        }
        // Password change
        if (request.currentPassword() != null && !request.currentPassword().isBlank()
                && request.newPassword() != null && !request.newPassword().isBlank()) {
            if (!passwordEncoder.matches(request.currentPassword(), dbUser.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mot de passe actuel incorrect."));
            }
            dbUser.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        userRepository.save(dbUser);

        Map<String, Object> updated = new HashMap<>();
        updated.put("userId", dbUser.getId());
        updated.put("email", dbUser.getEmail());
        updated.put("firstName", dbUser.getFirstName());
        updated.put("lastName", dbUser.getLastName());
        updated.put("telephone", dbUser.getTelephone());
        updated.put("message", "Profil mis à jour avec succès.");
        return ResponseEntity.ok(updated);
    }

    public record UpdateProfileRequest(
            String firstName,
            String lastName,
            String email,
            String telephone,
            String currentPassword,
            String newPassword) {}
}
