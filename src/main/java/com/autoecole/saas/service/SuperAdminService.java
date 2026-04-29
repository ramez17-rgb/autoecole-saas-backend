package com.autoecole.saas.service;

import com.autoecole.saas.security.PasswordSetupToken;
import com.autoecole.saas.security.PasswordSetupTokenRepository;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SuperAdminService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordSetupTokenRepository tokenRepository;
    private final EmailService emailService;

    public SuperAdminService(TenantRepository tenantRepository,
                             UserRepository userRepository,
                             PasswordSetupTokenRepository tokenRepository,
                             EmailService emailService) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String validateTenant(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (!"PENDING".equals(tenant.getStatus())) {
            throw new RuntimeException("Tenant is not in PENDING status");
        }

        // 1. Activate Tenant
        tenant.setStatus("ACTIVE");
        tenant.setValidatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);

        // 2. Activate Tenant Admin & Generate Token
        User admin = userRepository.findByTenantId(tenantId)
                .stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> "TENANT_ADMIN".equals(r.getName())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No admin user found for tenant"));

        admin.setStatus("active");
        userRepository.save(admin);

        // 3. Generate Secure Token
        String token = UUID.randomUUID().toString();
        System.out.println("GENERATED TOKEN: " + token); // For verification purposes
        PasswordSetupToken setupToken = new PasswordSetupToken();
        setupToken.setToken(token);
        setupToken.setUser(admin);
        setupToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        tokenRepository.save(setupToken);

        // 4. Send Email
        emailService.sendPasswordSetupEmail(admin.getEmail(), token);

        return token;
    }

    @Transactional
    public void rejectTenant(UUID tenantId, String reason) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        tenant.setStatus("REJECTED");
        tenant.setRefusalReason(reason);
        tenantRepository.save(tenant);

        // Find admin to send email? or use the contact email on tenant?
        // Let's use contact email on tenant as per my implementation of
        // TenantRegistrationService
        if (tenant.getContactEmail() != null) {
            emailService.sendRejectionEmail(tenant.getContactEmail(), reason);
        }
    }
}