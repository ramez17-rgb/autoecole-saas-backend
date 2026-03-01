package com.autoecole.saas.tenant;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public void approve(@PathVariable UUID id) {
        tenantApprovalService.approveTenant(id);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable UUID id, @RequestBody RejectRequest request) {
        tenantApprovalService.rejectTenant(id, request.reason());
    }

    public record RejectRequest(String reason) {
    }
}
