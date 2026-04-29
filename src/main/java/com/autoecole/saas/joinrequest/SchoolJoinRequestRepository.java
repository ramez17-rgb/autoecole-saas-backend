package com.autoecole.saas.joinrequest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SchoolJoinRequestRepository extends JpaRepository<SchoolJoinRequest, UUID> {
    List<SchoolJoinRequest> findByTenantIdAndStatus(UUID tenantId, JoinRequestStatus status);
    List<SchoolJoinRequest> findByStudentId(UUID studentId);
    boolean existsByStudentIdAndTenantIdAndStatus(UUID studentId, UUID tenantId, JoinRequestStatus status);
}