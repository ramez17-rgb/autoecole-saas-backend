package com.autoecole.saas.tenant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicSchoolController {

    private final TenantRepository tenantRepository;

    public PublicSchoolController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @GetMapping("/schools")
    public List<Map<String, Object>> getPublicSchools() {
        return tenantRepository.findAll().stream()
                .filter(t -> "ACTIVE".equalsIgnoreCase(t.getStatus()))
                .map(t -> {
                    Map<String, Object> school = new HashMap<>();
                    school.put("id", t.getId());
                    school.put("name", t.getName());
                    school.put("address", t.getAddress());
                    school.put("contactEmail", t.getContactEmail());
                    school.put("email", t.getContactEmail());
                    return school;
                })
                .collect(Collectors.toList());
    }
}
