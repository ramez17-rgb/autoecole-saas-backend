package com.autoecole.saas.auth;

import com.autoecole.saas.security.JwtService;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.autoecole.saas.security.PasswordSetupTokenRepository tokenRepository;

    public AuthController(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            com.autoecole.saas.security.PasswordSetupTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }

    @GetMapping
    public String check() {
        return "Auth Service is running";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new RuntimeException("User disabled");
        }

        if (user.getTenant() != null && !user.getTenant().isActive()) {
            throw new RuntimeException("Tenant is not active");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(user);
    }

    @PostMapping("/setup-password")
    public String setupPassword(@RequestParam String token, @RequestBody PasswordSetupRequest request) {
        com.autoecole.saas.security.PasswordSetupToken setupToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (setupToken.isExpired()) {
            throw new RuntimeException("Token expired");
        }

        if (setupToken.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        User user = setupToken.getUser();
        user.setPassword(passwordEncoder.encode(request.password()));
        // Ensure user is enabled if they weren't
        user.setStatus("active");
        userRepository.save(user);

        setupToken.setUsed(true);
        tokenRepository.save(setupToken);

        return "Password set successfully";
    }

    public record LoginRequest(String email, String password) {
    }

    public record PasswordSetupRequest(String password) {
    }
}
