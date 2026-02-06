package com.autoecole.saas.monitor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitorRepository extends JpaRepository<Monitor, UUID> {

    List<Monitor> findByTenantId(UUID tenantId);

    Optional<Monitor> findByIdAndTenantId(UUID id, UUID tenantId);
}
