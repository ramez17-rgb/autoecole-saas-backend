package com.autoecole.saas.controller;

import com.autoecole.saas.tenant.TenantRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
public class TenantRegistrationController {

    private final TenantRegistrationService registrationService;

    public TenantRegistrationController(TenantRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerTenant(
            @RequestBody TenantRegistrationRequest request) {
        registrationService.register(request.name(), request.email(), request.address());
        return ResponseEntity.ok("Inscription reçue. En attente de validation.");
    }

    public record TenantRegistrationRequest(String name, String email, String address) {
    }
}
