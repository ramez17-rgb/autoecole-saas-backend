package com.autoecole.saas.mock;

import com.autoecole.saas.security.PasswordSetupToken;
import com.autoecole.saas.security.PasswordSetupTokenRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;

@RestController
@RequestMapping("/api/dev")
public class DevHelperController {

    private final PasswordSetupTokenRepository tokenRepository;

    public DevHelperController(PasswordSetupTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @GetMapping("/last-token")
    public String getLastToken(@RequestParam String email) {
        return tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getEmail().equalsIgnoreCase(email))
                .filter(t -> !t.isUsed())
                .max(Comparator.comparing(PasswordSetupToken::getExpiresAt))
                .map(PasswordSetupToken::getToken)
                .orElse("No active token found");
    }
}
