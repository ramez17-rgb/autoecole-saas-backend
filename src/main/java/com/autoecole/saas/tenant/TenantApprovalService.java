package com.autoecole.saas.tenant;

import com.autoecole.saas.email.EmailService;
import com.autoecole.saas.security.PasswordSetupToken;
import com.autoecole.saas.security.PasswordSetupTokenRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TenantApprovalService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordSetupTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    public TenantApprovalService(TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordSetupTokenRepository tokenRepository,
            EmailService emailService) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String approveTenant(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        if (!"PENDING".equalsIgnoreCase(tenant.getStatus())) {
            throw new IllegalStateException("Tenant is not in PENDING status");
        }

        // 1. Mark Tenant as ACTIVE
        tenant.setStatus("ACTIVE");
        tenant.setValidatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);

        // 2. Find Admin User (the one created during registration)
        User adminUser = userRepository.findAdminByTenant(tenantId)
                .orElseThrow(() -> new IllegalStateException("Admin user not found for tenant"));

        // 3. Generate Password Setup Token
        PasswordSetupToken setupToken = new PasswordSetupToken();
        setupToken.setUser(adminUser);
        setupToken.setToken(UUID.randomUUID().toString());
        setupToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        tokenRepository.save(setupToken);

        // 4. Send Email
        String setupUrl = baseUrl + "/setup-password?token=" + setupToken.getToken();
        emailService.sendPasswordSetup(adminUser.getEmail(), setupUrl);

        return setupToken.getToken();
    }

    @Transactional
    public void rejectTenant(UUID tenantId, String reason) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        tenant.setStatus("REJECTED");
        tenant.setRefusalReason(reason);
        tenantRepository.save(tenant);
    }
}
