package com.autoecole.saas.controller;

import com.autoecole.saas.service.TenantRegistrationService;
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
            @RequestBody TenantRegistrationService.TenantRegistrationRequest request) {
        registrationService.registerTenant(request);
        return ResponseEntity.ok("Inscription reçue. En attente de validation.");
    }
}
