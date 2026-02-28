package com.autoecole.saas.security;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PasswordSetupTokenRepository extends JpaRepository<PasswordSetupToken, UUID> {
    Optional<PasswordSetupToken> findByToken(String token);
}
