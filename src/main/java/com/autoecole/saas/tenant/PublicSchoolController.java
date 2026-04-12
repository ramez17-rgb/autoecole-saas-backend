package com.autoecole.saas.tenant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/schools")
public class PublicSchoolController {

    private final TenantRepository tenantRepository;

    public PublicSchoolController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @GetMapping
    public List<PublicSchoolDto> getActiveSchools() {
        return tenantRepository.findByStatus("ACTIVE").stream()
                .map(t -> new PublicSchoolDto(t.getId(), t.getName(), t.getAddress()))
                .collect(Collectors.toList());
    }

    public record PublicSchoolDto(UUID id, String name, String address) {}
}
