package com.autoecole.saas.tenant;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/test")
public class controllertestuserndtenant {

    // Endpoint simple pour vérifier l'injection du tenantId
    @GetMapping("/whoami")
    public Map<String, Object> whoAmI(@RequestAttribute("tenantId") java.util.UUID tenantId) {
        Map<String, Object> response = new HashMap<>();
        response.put("tenantId", tenantId);
        response.put("message", "Tenant ID injected correctly!");
        return response;
    }

    // Endpoint pour lister des "users" factices par tenant
    @GetMapping("/users")
    public List<Map<String, Object>> getUsers(@RequestAttribute("tenantId") java.util.UUID tenantId) {
        List<Map<String, Object>> users = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", i);
            user.put("username", "user" + i + "_tenant" + tenantId);
            user.put("tenantId", tenantId);
            users.add(user);
        }
        return users;
    }

    // Endpoint pour créer un "user" factice
    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestAttribute("tenantId") java.util.UUID tenantId,
            @RequestBody Map<String, Object> body) {
        body.put("tenantId", tenantId); // Injection automatique
        body.put("id", new Random().nextInt(1000)); // ID fictif
        body.put("message", "User created for tenant " + tenantId);
        return body;
    }
}
