package com.autoecole.saas.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findByTenantId(UUID tenantId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.tenant.id = :tenantId AND r.name = 'ADMIN'")
    Optional<User> findAdminByTenant(@Param("tenantId") UUID tenantId);
}
