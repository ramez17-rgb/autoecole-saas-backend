package com.autoecole.saas.enrollment;

import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.tenant.TenantRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class EnrollmentController {

    private final EnrollmentRequestRepository enrollmentRequestRepository;
    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public EnrollmentController(EnrollmentRequestRepository enrollmentRequestRepository,
                                StudentRepository studentRepository,
                                TenantRepository tenantRepository,
                                UserRepository userRepository) {
        this.enrollmentRequestRepository = enrollmentRequestRepository;
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new RuntimeException("Unauthorized");
    }

    // ================= For Students =================

    @PostMapping("/api/enrollments/schools/{schoolId}")
    @Transactional
    public EnrollmentRequest applyToSchool(@PathVariable UUID schoolId) {
        User user = getAuthenticatedUser();
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        if (student.getTenant() != null) {
            throw new RuntimeException("You are already enrolled in a school.");
        }

        Tenant school = tenantRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        if (!school.isActive()) {
            throw new RuntimeException("School is not active.");
        }

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudent(student);
        request.setTenant(school);
        request.setStatus(EnrollmentStatus.PENDING);
        
        return enrollmentRequestRepository.save(request);
    }

    @GetMapping("/api/enrollments/me")
    public List<EnrollmentRequest> getMyEnrollments() {
        User user = getAuthenticatedUser();
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
        return enrollmentRequestRepository.findByStudentId(student.getId());
    }

    // ================= For Tenant Admins =================

    @GetMapping("/api/admin/enrollments")
    public List<EnrollmentRequest> getPendingEnrollments() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("No tenant in context");
        }
        return enrollmentRequestRepository.findByTenantIdAndStatus(tenantId, EnrollmentStatus.PENDING);
    }

    @PostMapping("/api/admin/enrollments/{enrollmentId}/accept")
    @Transactional
    public void acceptEnrollment(@PathVariable UUID enrollmentId) {
        UUID tenantId = TenantContext.getTenantId();
        EnrollmentRequest request = enrollmentRequestRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        if (!request.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Not authorized for this tenant");
        }
        
        request.setStatus(EnrollmentStatus.APPROVED);
        enrollmentRequestRepository.save(request);

        Student student = request.getStudent();
        student.setTenant(request.getTenant());
        studentRepository.save(student);

        User user = student.getUser();
        user.setTenant(request.getTenant());
        userRepository.save(user);
    }

    @PostMapping("/api/admin/enrollments/{enrollmentId}/reject")
    @Transactional
    public void rejectEnrollment(@PathVariable UUID enrollmentId) {
        UUID tenantId = TenantContext.getTenantId();
        EnrollmentRequest request = enrollmentRequestRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Not authorized for this tenant");
        }

        request.setStatus(EnrollmentStatus.REJECTED);
        enrollmentRequestRepository.save(request);
    }
}
