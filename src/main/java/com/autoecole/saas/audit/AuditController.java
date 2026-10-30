package com.autoecole.saas.audit;

import com.autoecole.saas.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            // Super Admin: sees all logs with pagination
            return ResponseEntity.ok(auditService.getAllLogsPaged(page, size));
        } else {
            // Tenant: sees only their logs
            return ResponseEntity.ok(auditService.getLogsByTenantPaged(tenantId, page, size));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getAuditCount() {
        UUID tenantId = TenantContext.getTenantId();
        long total = tenantId == null
                ? auditService.getTotalCount()
                : auditService.getTenantCount(tenantId);
        return ResponseEntity.ok(Map.of("total", total));
    }
}
