package com.autoecole.saas.monitor;

import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public MonitorService(MonitorRepository monitorRepository,
                          TenantRepository tenantRepository,
                          UserRepository userRepository) {
        this.monitorRepository = monitorRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    // CREATE
    public Monitor create(UUID tenantId, UUID userId) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Monitor monitor = new Monitor();
        monitor.setTenant(tenant);
        monitor.setUser(user);

        return monitorRepository.save(monitor);
    }

    // READ ALL
    public List<Monitor> getAll(UUID tenantId) {
        return monitorRepository.findByTenantId(tenantId);
    }

    // READ ONE
    public Monitor getOne(UUID tenantId, UUID id) {
        return monitorRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Monitor not found"));
    }

    // DELETE
    public void delete(UUID tenantId, UUID id) {
        Monitor monitor = getOne(tenantId, id);
        monitorRepository.delete(monitor);
    }
}
