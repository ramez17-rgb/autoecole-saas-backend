package com.autoecole.saas.monitor;

import com.autoecole.saas.monitor.dto.CreateMonitorRequest;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.tenant.TenantRepository;
import com.autoecole.saas.user.RoleEntity;
import com.autoecole.saas.user.RoleRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MonitorService {

    private final UserRepository userRepository;
    private final MonitorRepository monitorRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public MonitorService(
            UserRepository userRepository,
            MonitorRepository monitorRepository,
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.monitorRepository = monitorRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Monitor createMonitor(CreateMonitorRequest request) {

        UUID tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // 1️⃣ Create USER
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Handle Role
        RoleEntity role = roleRepository.findByName("MONITOR")
                .orElseGet(() -> roleRepository.save(new RoleEntity("MONITOR")));
        user.getRoles().add(role);

        user.setTenant(tenant);
        user.setStatus("active");

        userRepository.save(user);

        // 2️⃣ Create MONITOR
        Monitor monitor = new Monitor();
        monitor.setUser(user);
        monitor.setTenant(tenant);

        return monitorRepository.save(monitor);
    }

    public List<Monitor> getAllMonitors() {
        return monitorRepository.findByTenantId(TenantContext.getTenantId());
    }

    public Monitor getMonitor(UUID id) {
        // Ensure the monitor belongs to the current tenant
        return monitorRepository.findById(id)
                .filter(m -> m.getTenant().getId().equals(TenantContext.getTenantId()))
                .orElseThrow(() -> new RuntimeException("Monitor not found"));
    }

}
