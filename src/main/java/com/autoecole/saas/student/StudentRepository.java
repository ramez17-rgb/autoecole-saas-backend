package com.autoecole.saas.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.user WHERE s.tenant.id = :tenantId")
    List<Student> findByTenantId(UUID tenantId);

    Optional<Student> findByIdAndTenantId(UUID id, UUID tenantId);
}
