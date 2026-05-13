package com.autoecole.saas.auth;

import com.autoecole.saas.security.JwtService;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.autoecole.saas.security.PasswordSetupTokenRepository tokenRepository;
    private final com.autoecole.saas.email.EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${app.base-url}")
    private String baseUrl;

    public AuthController(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            com.autoecole.saas.security.PasswordSetupTokenRepository tokenRepository,
            com.autoecole.saas.email.EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @GetMapping
    public String check() {
        return "Auth Service is running";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User disabled");
        }

        if (user.getTenant() != null && !user.getTenant().isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant is not active");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
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

        if (!user.getEmail().equalsIgnoreCase(request.email())) {
            throw new RuntimeException("Email does not match the setup link");
        }

        user.setPassword(passwordEncoder.encode(request.password()));
        // Ensure user is enabled if they weren't
        user.setStatus("active");
        user.setEnabled(true);
        userRepository.save(user);

        setupToken.setUsed(true);
        tokenRepository.save(setupToken);

        return "Password set successfully";
    }

    @PostMapping("/resend-setup-link")
    public String resendSetupLink(@RequestBody ResendLinkRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTenant() == null || !"ACTIVE".equalsIgnoreCase(user.getTenant().getStatus())) {
            throw new RuntimeException("Your school is not yet approved.");
        }

        // Generate a new token
        com.autoecole.saas.security.PasswordSetupToken newToken = new com.autoecole.saas.security.PasswordSetupToken();
        newToken.setUser(user);
        newToken.setToken(java.util.UUID.randomUUID().toString());
        newToken.setExpiresAt(java.time.LocalDateTime.now().plusHours(24));
        tokenRepository.save(newToken);

        String setupUrl = baseUrl + "/setup-password?token=" + newToken.getToken();
        emailService.sendPasswordSetup(user.getEmail(), setupUrl);

        return "Setup link sent to " + request.email();
    }

    /**
     * POST /api/auth/change-password
     * Used by tenants who logged in with a provisional password.
     * Verifies the current (provisional) password, sets the new one, and clears mustChangePassword.
     */
    @PostMapping("/change-password")
    public String changePassword(
            @RequestBody ChangePasswordRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        // Extract email from JWT
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        return "Password changed successfully";
    }

    public record LoginRequest(String email, String password) {
    }

    public record PasswordSetupRequest(String email, String password) {
    }

    public record ResendLinkRequest(String email) {
    }

    public record ChangePasswordRequest(String currentPassword, String newPassword) {
    }
}
