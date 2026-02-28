package com.autoecole.saas.tenant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantRegistrationService tenantRegistrationService;

    public TenantController(TenantRegistrationService tenantRegistrationService) {
        this.tenantRegistrationService = tenantRegistrationService;
    }

    /**
     * Public endpoint: register a new auto-école.
     * Creates the Tenant + a disabled Admin user + returns a password setup token.
     * In production the token would be emailed; here it's returned in the response for testing.
     */
    @PostMapping("/register")
    public ResponseEntity<TenantRegistrationResponse> register(@Valid @RequestBody TenantRegistrationRequest request) {
        try {
            TenantRegistrationService.TenantRegistrationResult result =
                    tenantRegistrationService.register(request.getName(), request.getEmail(), request.getAddress());

            TenantRegistrationResponse response = new TenantRegistrationResponse(
                    result.tenant().getId(),
                    result.tenant().getName(),
                    result.adminUser().getEmail(),
                    result.setupToken()
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── Request DTO ────────────────────────────────────────────────────
    public static class TenantRegistrationRequest {
        @NotBlank(message = "School name is required")
        private String name;

        @Email(message = "Admin email must be valid")
        @NotBlank(message = "Admin email is required")
        private String email;

        private String address;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    // ── Response DTO ───────────────────────────────────────────────────
    public static class TenantRegistrationResponse {
        private java.util.UUID tenantId;
        private String tenantName;
        private String adminEmail;
        private String setupToken; // Use this with POST /api/auth/setup-password?token=<value>

        public TenantRegistrationResponse(java.util.UUID tenantId, String tenantName,
                                          String adminEmail, String setupToken) {
            this.tenantId = tenantId;
            this.tenantName = tenantName;
            this.adminEmail = adminEmail;
            this.setupToken = setupToken;
        }

        public java.util.UUID getTenantId() { return tenantId; }
        public String getTenantName() { return tenantName; }
        public String getAdminEmail() { return adminEmail; }
        public String getSetupToken() { return setupToken; }
    }
}
