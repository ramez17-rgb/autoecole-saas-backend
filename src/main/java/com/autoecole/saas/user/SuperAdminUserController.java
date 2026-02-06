package com.autoecole.saas.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/users")
public class SuperAdminUserController {

    private final UserService userService;

    public SuperAdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsersGlobal() {
        return userService.getAllUsersGlobal();
    }
}
