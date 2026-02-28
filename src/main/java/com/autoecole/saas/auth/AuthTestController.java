package com.autoecole.saas.auth;

import com.autoecole.saas.security.JwtService;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.user.RoleEntity;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.RoleRepository;
import com.autoecole.saas.monitor.Monitor;
import com.autoecole.saas.monitor.MonitorRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthTestController {

    private final JwtService jwtService;

    private final com.autoecole.saas.user.UserRepository userRepository;
    private final com.autoecole.saas.tenant.TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final MonitorRepository monitorRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AuthTestController(JwtService jwtService,
            com.autoecole.saas.user.UserRepository userRepository,
            com.autoecole.saas.tenant.TenantRepository tenantRepository,
            RoleRepository roleRepository,
            MonitorRepository monitorRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.monitorRepository = monitorRepository;
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
        tenant.setStatus("ACTIVE");
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

    @org.springframework.web.bind.annotation.PostMapping("/api/auth/seed-monitor")
    public String createMonitorUser() {
        if (userRepository.findByEmail("monitor@test.com").isPresent()) {
            return "Monitor User already exists";
        }

        // Reuse or create Tenant
        Tenant tenant = tenantRepository.findAll().stream().findFirst().orElseGet(() -> {
            Tenant newTenant = new Tenant();
            newTenant.setName("Test AutoEcole");
            newTenant.setStatus("ACTIVE");
            return tenantRepository.save(newTenant);
        });

        RoleEntity monitorRole = roleRepository.findByName("MONITOR")
                .orElseGet(() -> roleRepository.save(new RoleEntity("MONITOR")));

        User user = new User();
        user.setEmail("monitor@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.getRoles().add(monitorRole);
        user.setTenant(tenant);
        user.setEnabled(true);

        try {
            user = userRepository.save(user); // Save user first to get ID

            Monitor monitor = new Monitor();
            monitor.setTenant(tenant);
            monitor.setUser(user);
            monitorRepository.save(monitor);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }

        return "Monitor created: monitor@test.com / password";
    }

    @org.springframework.web.bind.annotation.PostMapping("/api/auth/seed-super-admin")
    public String createSuperAdmin() {
        if (userRepository.findByEmail("superadmin@test.com").isPresent()) {
            return "Super Admin already exists";
        }

        RoleEntity superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> roleRepository.save(new RoleEntity("SUPER_ADMIN")));

        User user = new User();
        user.setEmail("superadmin@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.getRoles().add(superAdminRole);
        user.setTenant(null); // Global user
        user.setEnabled(true);

        userRepository.save(user);

        return "Super Admin created: superadmin@test.com / password";
    }
}