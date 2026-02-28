package com.autoecole.saas.exam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {
    List<Exam> findByTenantId(UUID tenantId);

    List<Exam> findByTenantIdAndStudentId(UUID tenantId, UUID studentId);

    List<Exam> findByTenantIdAndStatus(UUID tenantId, String status);
}
