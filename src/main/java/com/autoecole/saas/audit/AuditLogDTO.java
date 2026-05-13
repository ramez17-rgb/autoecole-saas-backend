package com.autoecole.saas.audit;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLogDTO {

    private UUID id;
    private UUID tenantId;
    private String tenantName;
    private UUID userId;
    private String userName;
    private String action;
    private LocalDateTime timestamp;

    public AuditLogDTO() {}

    public AuditLogDTO(AuditLog log, String tenantName, String userName) {
        this.id = log.getId();
        this.tenantId = log.getTenantId();
        this.tenantName = tenantName;
        this.userId = log.getUserId();
        this.userName = userName;
        this.action = log.getAction();
        this.timestamp = log.getTimestamp();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
