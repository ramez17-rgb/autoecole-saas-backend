package com.autoecole.saas.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EnrollmentRequestRepository extends JpaRepository<EnrollmentRequest, UUID> {
    List<EnrollmentRequest> findByTenantIdAndStatus(UUID tenantId, EnrollmentStatus status);
    List<EnrollmentRequest> findByStudentId(UUID studentId);
}
