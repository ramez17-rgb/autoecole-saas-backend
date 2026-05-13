package com.autoecole.saas.tenant;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/tenants")
public class SuperAdminTenantController {

    private final TenantRepository tenantRepository;
    private final TenantApprovalService tenantApprovalService;

    public SuperAdminTenantController(TenantRepository tenantRepository,
            TenantApprovalService tenantApprovalService) {
        this.tenantRepository = tenantRepository;
        this.tenantApprovalService = tenantApprovalService;
    }

    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    /**
     * Approve a tenant.
     * Body: { "provisionalPassword": "..." }  — optional.
     * If provisionalPassword is provided and non-blank, it is set immediately and
     * the tenant admin must change it on first login (mustChangePassword=true).
     * If omitted / blank, an email setup link is sent instead.
     */
    @PostMapping("/{id}/approve")
    public TenantApprovalResponse approve(
            @PathVariable java.util.UUID id,
            @RequestBody(required = false) ApproveRequest request) {

        String provisionalPassword = (request != null) ? request.provisionalPassword() : null;
        String token = tenantApprovalService.approveTenant(id, provisionalPassword);

        boolean passwordSet = (provisionalPassword != null && !provisionalPassword.isBlank());

        return new TenantApprovalResponse(
                passwordSet
                        ? "Tenant approved with provisional password. The tenant must change it on first login."
                        : "Tenant approved. A password-setup email has been sent.",
                token,
                token != null ? "/api/auth/setup-password?token=" + token : null,
                passwordSet);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable java.util.UUID id, @RequestBody RejectRequest request) {
        tenantApprovalService.rejectTenant(id, request.reason());
    }

    public record ApproveRequest(String provisionalPassword) {}

    public record RejectRequest(String reason) {}

    public record TenantApprovalResponse(
            String message,
            String setupToken,
            String exampleSetupUrl,
            boolean provisionalPasswordSet) {}
}
