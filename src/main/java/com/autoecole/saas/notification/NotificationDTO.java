package com.autoecole.saas.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDTO {

    private UUID id;
    private UUID tenantId;
    private UUID studentId;
    private String studentName;
    private UUID requestId;
    private String requestType;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationDTO() {}

    public NotificationDTO(Notification n) {
        this.id = n.getId();
        this.tenantId = n.getTenantId();
        this.studentId = n.getStudentId();
        this.studentName = n.getStudentName();
        this.requestId = n.getRequestId();
        this.requestType = n.getRequestType();
        this.message = n.getMessage();
        this.read = n.isRead();
        this.createdAt = n.getCreatedAt();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public UUID getRequestId() { return requestId; }
    public String getRequestType() { return requestType; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(UUID id) { this.id = id; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public void setMessage(String message) { this.message = message; }
    public void setRead(boolean read) { this.read = read; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
