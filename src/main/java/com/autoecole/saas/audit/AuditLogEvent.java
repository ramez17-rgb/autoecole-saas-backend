package com.autoecole.saas.audit;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class AuditLogEvent extends ApplicationEvent {
    private final UUID tenantId;
    private final UUID userId;
    private final String action;

    public AuditLogEvent(Object source, UUID tenantId, UUID userId, String action) {
        super(source);
        this.tenantId = tenantId;
        this.userId = userId;
        this.action = action;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }
}
