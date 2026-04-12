package com.autoecole.saas.joinrequest;

import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/join-requests")
public class SchoolJoinRequestController {

    private final SchoolJoinRequestRepository joinRequestRepository;
    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public SchoolJoinRequestController(
            SchoolJoinRequestRepository joinRequestRepository,
            StudentRepository studentRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository) {
        this.joinRequestRepository = joinRequestRepository;
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    // ─── PUBLIC: list all active schools (for student to browse) ───────────────
    @GetMapping("/schools")
    public List<SchoolDTO> listSchools() {
        return tenantRepository.findAll().stream()
                .filter(Tenant::isActive)
                .map(t -> new SchoolDTO(t.getId(), t.getName(), t.getAddress(), t.getContactEmail()))
                .collect(Collectors.toList());
    }

    // ─── STUDENT: send a join request ──────────────────────────────────────────
    @PostMapping("/request")
    @Transactional
    public String requestJoin(@AuthenticationPrincipal User user,
                              @RequestParam UUID tenantId) {
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));

        if (!tenant.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "School is not active");
        }

        boolean alreadyPending = joinRequestRepository
                .existsByStudentIdAndTenantIdAndStatus(student.getId(), tenantId, JoinRequestStatus.PENDING);
        if (alreadyPending) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have a pending request for this school");
        }

        SchoolJoinRequest request = new SchoolJoinRequest();
        request.setStudent(student);
        request.setTenant(tenant);
        joinRequestRepository.save(request);

        return "Request sent successfully";
    }

    // ─── TENANT ADMIN: view pending requests for their school ──────────────────
    @GetMapping("/pending")
    public List<JoinRequestDTO> getPendingRequests(@AuthenticationPrincipal User user) {
        Tenant tenant = user.getTenant();
        if (tenant == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenant associated");
        }

        return joinRequestRepository
                .findByTenantIdAndStatus(tenant.getId(), JoinRequestStatus.PENDING)
                .stream()
                .map(r -> new JoinRequestDTO(
                        r.getId(),
                        r.getStudent().getId(),
                        r.getStudent().getPrenom() + " " + r.getStudent().getNom(),
                        r.getStudent().getUser().getEmail(),
                        r.getStatus().name(),
                        r.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // ─── TENANT ADMIN: approve a request ───────────────────────────────────────
    @PostMapping("/{requestId}/approve")
    @Transactional
    public String approve(@PathVariable UUID requestId,
                          @AuthenticationPrincipal User user) {
        SchoolJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!request.getTenant().getId().equals(user.getTenant().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school");
        }

        request.setStatus(JoinRequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        joinRequestRepository.save(request);

        // Assign tenant to student
        Student student = request.getStudent();
        student.setTenant(request.getTenant());
        studentRepository.save(student);

        // Also assign tenant to user
        student.getUser().setTenant(request.getTenant());
        userRepository.save(student.getUser());

        return "Student approved and assigned to your school";
    }

    // ─── TENANT ADMIN: reject a request ────────────────────────────────────────
    @PostMapping("/{requestId}/reject")
    @Transactional
    public String reject(@PathVariable UUID requestId,
                         @AuthenticationPrincipal User user,
                         @RequestParam(required = false) String reason) {
        SchoolJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!request.getTenant().getId().equals(user.getTenant().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your school");
        }

        request.setStatus(JoinRequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setRejectionReason(reason);
        joinRequestRepository.save(request);

        return "Request rejected";
    }

    // ─── DTOs ───────────────────────────────────────────────────────────────────
    public record SchoolDTO(UUID id, String name, String address, String contactEmail) {}
    public record JoinRequestDTO(UUID requestId, UUID studentId, String fullName, String email, String status, LocalDateTime createdAt) {}
}