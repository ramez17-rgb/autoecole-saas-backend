package com.autoecole.saas.student;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    List<Student> findByTenantId(UUID tenantId);
}
