package com.autoecole.saas.audit;

import com.autoecole.saas.tenant.TenantRepository;
import com.autoecole.saas.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository,
                        TenantRepository tenantRepository,
                        UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void log(UUID tenantId, UUID userId, String action) {
        AuditLog auditLog = new AuditLog(tenantId, userId, action);
        auditLogRepository.save(auditLog);
    }

    public List<AuditLogDTO> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuditLogDTO> getLogsByTenant(UUID tenantId) {
        return auditLogRepository.findByTenantIdOrderByTimestampDesc(tenantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private AuditLogDTO toDTO(AuditLog log) {
        String tenantName = null;
        if (log.getTenantId() != null) {
            tenantName = tenantRepository.findById(log.getTenantId())
                    .map(t -> t.getName())
                    .orElse(log.getTenantId().toString().substring(0, 8) + "...");
        }

        String userName = null;
        if (log.getUserId() != null) {
            userName = userRepository.findById(log.getUserId())
                    .map(u -> {
                        String first = u.getFirstName() != null ? u.getFirstName() : "";
                        String last = u.getLastName() != null ? u.getLastName() : "";
                        String full = (first + " " + last).trim();
                        return full.isEmpty() ? u.getEmail() : full;
                    })
                    .orElse(log.getUserId().toString().substring(0, 8) + "...");
        }

        return new AuditLogDTO(log, tenantName, userName);
    }
}
