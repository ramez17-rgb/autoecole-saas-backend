package com.autoecole.saas.mock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MockTestRepository extends JpaRepository<MockTest, UUID> {
    List<MockTest> findByTenantIdAndStudentId(UUID tenantId, UUID studentId);

    List<MockTest> findByTenantId(UUID tenantId);
}
