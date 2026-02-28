package com.autoecole.saas.service;

import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantRepository;
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

    public TenantRegistrationService(TenantRepository tenantRepository, UserRepository userRepository,
            RoleRepository roleRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void registerTenant(TenantRegistrationRequest request) {
        // 1. Create Tenant (Pending)
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setContactEmail(request.getEmail());
        tenant.setAddress(request.getAddress());
        tenant.setStatus("PENDING");

        tenant = tenantRepository.save(tenant);

        // 2. Create Tenant Admin User (Disabled, No Password)
        RoleEntity tenantAdminRole = roleRepository.findByName("TENANT_ADMIN")
                .orElseGet(() -> roleRepository.save(new RoleEntity("TENANT_ADMIN")));

        User adminUser = new User();
        adminUser.setEmail(request.getEmail());
        adminUser.setTenant(tenant);
        adminUser.setStatus("disabled"); // Will be activated after validation
        adminUser.setPassword(""); // No password yet
        adminUser.getRoles().add(tenantAdminRole);

        userRepository.save(adminUser);
    }

    public static class TenantRegistrationRequest {
        private String name;
        private String email;
        private String address;

        // getters setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
