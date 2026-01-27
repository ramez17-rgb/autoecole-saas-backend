package com.autoecole.saas.auth;

import com.autoecole.saas.security.JwtService;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.user.RoleEntity;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.RoleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthTestController {

    private final JwtService jwtService;

    private final com.autoecole.saas.user.UserRepository userRepository;
    private final com.autoecole.saas.tenant.TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AuthTestController(JwtService jwtService,
            com.autoecole.saas.user.UserRepository userRepository,
            com.autoecole.saas.tenant.TenantRepository tenantRepository,
            RoleRepository roleRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/api/auth/test-token")
    public String testToken() {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());

        RoleEntity adminRole = new RoleEntity("ADMIN");

        User user = new User();
        user.setEmail("admin@test.com");
        user.getRoles().add(adminRole);
        user.setTenant(tenant);

        return jwtService.generateToken(user);
    }

    @org.springframework.web.bind.annotation.PostMapping("/api/auth/seed")
    public String createTestUser() {
        if (userRepository.findByEmail("admin@test.com").isPresent()) {
            return "User already exists";
        }

        Tenant tenant = new Tenant();
        tenant.setName("Test AutoEcole");
        tenant.setActive(true);
        tenant = tenantRepository.save(tenant);

        RoleEntity adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new RoleEntity("ADMIN")));

        User user = new User();
        user.setEmail("admin@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.getRoles().add(adminRole);
        user.setTenant(tenant);
        user.setEnabled(true);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }

        return "User created: admin@test.com / password";
    }
}