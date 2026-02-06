package com.autoecole.saas.monitor;

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

    // CREATE
    @PostMapping
    public Monitor create(
            @RequestAttribute("tenantId") UUID tenantId,
            @RequestParam UUID userId
    ) {
        return monitorService.create(tenantId, userId);
    }

    // READ ALL
    @GetMapping
    public List<Monitor> getAll(
            @RequestAttribute("tenantId") UUID tenantId
    ) {
        return monitorService.getAll(tenantId);
    }

    // READ ONE
    @GetMapping("/{id}")
    public Monitor getOne(
            @RequestAttribute("tenantId") UUID tenantId,
            @PathVariable UUID id
    ) {
        return monitorService.getOne(tenantId, id);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(
            @RequestAttribute("tenantId") UUID tenantId,
            @PathVariable UUID id
    ) {
        monitorService.delete(tenantId, id);
    }
}
