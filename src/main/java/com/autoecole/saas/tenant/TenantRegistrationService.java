package com.autoecole.saas.tenant;

import com.autoecole.saas.user.RoleEntity;
import com.autoecole.saas.user.RoleRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantRegistrationService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public TenantRegistrationService(TenantRepository tenantRepository,
            UserRepository userRepository,
            RoleRepository roleRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public TenantRegistrationResult register(String tenantName, String adminEmail, String address) {

        // Guard: prevent duplicate admin emails
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            throw new IllegalArgumentException("Email is already taken: " + adminEmail);
        }

        // 1. Create the Tenant
        Tenant tenant = new Tenant();
        tenant.setName(tenantName);
        tenant.setContactEmail(adminEmail);
        tenant.setAddress(address);
        tenant.setStatus("PENDING");
        tenant = tenantRepository.save(tenant);

        // 2. Create the ADMIN Role if needed
        RoleEntity adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new RoleEntity("ADMIN")));

        // 3. Create Admin User (disabled until password is set)
        User adminUser = new User();
        adminUser.setEmail(adminEmail);
        adminUser.setPassword("PENDING"); // placeholder — no real password yet
        adminUser.getRoles().add(adminRole);
        adminUser.setTenant(tenant);
        adminUser.setEnabled(false); // must set password first
        adminUser = userRepository.save(adminUser);

        return new TenantRegistrationResult(tenant, adminUser);
    }

    public record TenantRegistrationResult(Tenant tenant, User adminUser) {
    }
}
