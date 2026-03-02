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

    @PostMapping("/{id}/approve")
    public TenantApprovalResponse approve(@PathVariable java.util.UUID id) {
        String token = tenantApprovalService.approveTenant(id);
        return new TenantApprovalResponse(
                "Tenant approved successfully",
                token,
                "/api/auth/setup-password?token=" + token);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable java.util.UUID id, @RequestBody RejectRequest request) {
        tenantApprovalService.rejectTenant(id, request.reason());
    }

    public record RejectRequest(String reason) {
    }

    public record TenantApprovalResponse(String message, String setupToken, String exampleSetupUrl) {
    }
}
