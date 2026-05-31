package com.autoecole.saas.monitor;

import com.autoecole.saas.monitor.dto.CreateMonitorRequest;
import com.autoecole.saas.student.Student;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/monitors")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping
    public Monitor create(@RequestBody CreateMonitorRequest request) {
        return monitorService.createMonitor(request);
    }

    @GetMapping
    public List<Monitor> getAllMonitors() {
        return monitorService.getAllMonitors();
    }

    @GetMapping("/{id}")
    public Monitor getMonitor(@PathVariable UUID id) {
        return monitorService.getMonitor(id);
    }

}

@RestController
@RequestMapping("/api/monitors")
class PublicMonitorController {

    private final MonitorService monitorService;

    public PublicMonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping("/register")
    public Monitor register(@RequestBody PublicMonitorRegistrationRequest request) {
        return monitorService.registerPublicMonitor(request.firstName(), request.lastName(), request.email(), request.password(), request.telephone());
    }

    public record PublicMonitorRegistrationRequest(String firstName, String lastName, String email, String password, String telephone) {}
}
