package com.autoecole.saas.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    // Check if a tenant with this contact email already exists (if needed for
    // registration)
    // Optional<Tenant> findByContactEmail(String contactEmail);
    java.util.List<Tenant> findByStatus(String status);
}
