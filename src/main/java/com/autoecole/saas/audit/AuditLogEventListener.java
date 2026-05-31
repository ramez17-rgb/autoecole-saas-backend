package com.autoecole.saas.audit;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditLogEventListener {

    private final AuditService auditService;

    public AuditLogEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    @Async
    public void handleAuditLogEvent(AuditLogEvent event) {
        auditService.log(event.getTenantId(), event.getUserId(), event.getAction());
    }
}
