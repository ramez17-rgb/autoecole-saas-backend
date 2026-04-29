package com.autoecole.saas.user;

import com.autoecole.saas.tenant.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final com.autoecole.saas.tenant.TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            com.autoecole.saas.tenant.TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserRequest request) {

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTelephone(Integer.valueOf(request.getTelephone()));

        RoleEntity roleEntity = roleRepository.findByName(request.getRole().name())
                .orElseGet(() -> roleRepository.save(new RoleEntity(request.getRole().name())));

        user.getRoles().add(roleEntity);

        java.util.UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            com.autoecole.saas.tenant.Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));
            user.setTenant(tenant);
        }

        return userRepository.save(user);
    }

    public java.util.List<User> getAllUsersGlobal() {
        return userRepository.findAll();
    }

    public java.util.List<User> getUsersForCurrentTenant() {
        return userRepository.findByTenantId(TenantContext.getTenantId());
    }
}
