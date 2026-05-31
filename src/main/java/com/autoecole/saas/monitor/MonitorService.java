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
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public MonitorService(
            UserRepository userRepository,
            MonitorRepository monitorRepository,
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.monitorRepository = monitorRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Monitor createMonitor(CreateMonitorRequest request) {

        UUID tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // 1️⃣ Create USER
        User user = new User();
        user.setFirstName(request.getPrenom());
        user.setLastName(request.getNom());
        user.setEmail(request.getEmail());
        user.setTelephone(request.getTelephone());
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
        monitor.setTelephone(request.getTelephone());

        Monitor savedMonitor = monitorRepository.save(monitor);
        String monitorName = user.getFirstName() + " " + user.getLastName();
        eventPublisher.publishEvent(new com.autoecole.saas.audit.AuditLogEvent(this, tenantId, user.getId(), "Moniteur créé : " + monitorName));
        
        return savedMonitor;
    }

    @Transactional
    public Monitor registerPublicMonitor(String firstName, String lastName, String email, String password,
            String telephone) {
        // Find first active tenant since no tenant context is provided
        Tenant tenant = tenantRepository.findAll().stream()
                .filter(Tenant::isActive)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active tenant available for registration"));

        // Check if email is already taken
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email is already taken: " + email);
        }

        // 1️⃣ Create USER
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setTelephone(telephone);
        user.setPassword(passwordEncoder.encode(password));
        user.setTenant(tenant);
        user.setStatus("active");

        // Handle Role
        RoleEntity role = roleRepository.findByName("MONITOR")
                .orElseGet(() -> roleRepository.save(new RoleEntity("MONITOR")));
        user.getRoles().add(role);

        userRepository.save(user);

        // 2️⃣ Create MONITOR
        Monitor monitor = new Monitor();
        monitor.setUser(user);
        monitor.setTenant(tenant);
        monitor.setTelephone(telephone);

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
