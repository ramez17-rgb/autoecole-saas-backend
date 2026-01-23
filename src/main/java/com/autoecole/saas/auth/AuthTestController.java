package com.autoecole.saas.auth;

import com.autoecole.saas.security.JwtService;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.user.Role;
import com.autoecole.saas.user.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthTestController {

    private final JwtService jwtService;

    public AuthTestController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/api/auth/test-token")
    public String testToken() {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());

        User user = new User();
        user.setEmail("admin@test.com");
        user.setRole(Role.ADMIN);
        user.setTenant(tenant);

        return jwtService.generateToken(user);
    }
}