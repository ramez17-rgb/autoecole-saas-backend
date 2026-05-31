package com.autoecole.saas.tenant;

import com.autoecole.saas.email.EmailService;
import com.autoecole.saas.security.PasswordSetupToken;
import com.autoecole.saas.security.PasswordSetupTokenRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    public TenantApprovalService(TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordSetupTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Approve a tenant without a provisional password → sends an email setup link.
     */
    @Transactional
    public String approveTenant(UUID tenantId) {
        return approveTenant(tenantId, null);
    }

    /**
     * Approve a tenant.
     * - If provisionalPassword is non-blank: encode it, store it, mark mustChangePassword=true.
     *   Returns null (no email token needed).
     * - Otherwise: generate a password-setup token and send it by email.
     *   Returns the token string.
     */
    @Transactional
    public String approveTenant(UUID tenantId, String provisionalPassword) {
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

        if (provisionalPassword != null && !provisionalPassword.isBlank()) {
            // 3a. Provisional password path — tenant must change password on first login
            adminUser.setPassword(passwordEncoder.encode(provisionalPassword));
            adminUser.setStatus("active");
            adminUser.setEnabled(true);
            adminUser.setMustChangePassword(true);
            userRepository.save(adminUser);
            return null; // no setup token needed
        } else {
            // 3b. Email-link path — generate token and send email
            PasswordSetupToken setupToken = new PasswordSetupToken();
            setupToken.setUser(adminUser);
            setupToken.setToken(UUID.randomUUID().toString());
            setupToken.setExpiresAt(LocalDateTime.now().plusHours(24));
            tokenRepository.save(setupToken);

            String setupUrl = baseUrl + "/setup-password?token=" + setupToken.getToken();
            emailService.sendPasswordSetup(adminUser.getEmail(), setupUrl);

            return setupToken.getToken();
        }
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
