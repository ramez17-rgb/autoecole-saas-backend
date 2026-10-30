package com.autoecole.saas.tenant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TenantTestController {

    @GetMapping("/api/test/tenant")
    public UUID getTenant() {
        return TenantContext.getTenantId();
    }
}