package com.autoecole.saas.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentContractRepository extends JpaRepository<StudentContract, UUID> {
    Optional<StudentContract> findByStudentId(UUID studentId);

    List<StudentContract> findByTenantId(UUID tenantId);
}
