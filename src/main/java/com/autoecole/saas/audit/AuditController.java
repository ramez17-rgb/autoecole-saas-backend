package com.autoecole.saas.audit;

import com.autoecole.saas.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    @Autowired
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs() {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            // Super Admin: sees all logs
            return ResponseEntity.ok(auditService.getAllLogs());
        } else {
            // Tenant: sees only their logs
            return ResponseEntity.ok(auditService.getLogsByTenant(tenantId));
        }
    }
}
