package com.autoecole.saas.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(UUID tenantId, UUID userId, String action) {
        AuditLog auditLog = new AuditLog(tenantId, userId, action);
        auditLogRepository.save(auditLog);
    }
}
