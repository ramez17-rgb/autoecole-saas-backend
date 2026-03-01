package com.autoecole.saas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("Applying database fix: Dropping NOT NULL constraint on users.tenant_id");
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN tenant_id DROP NOT NULL");
            System.out.println("Database fix (users.tenant_id) applied successfully.");
        } catch (Exception e) {
            System.out.println("Database fix (users.tenant_id) failed: " + e.getMessage());
        }

        try {
            System.out.println(
                    "Applying database fix: Dropping conflicting expires_at column from password_setup_tokens");
            jdbcTemplate.execute("ALTER TABLE password_setup_tokens DROP COLUMN IF EXISTS expires_at");
            System.out.println("Database fix (password_setup_tokens.expires_at) applied successfully.");
        } catch (Exception e) {
            System.out.println("Database fix (password_setup_tokens.expires_at) failed: " + e.getMessage());
        }
    }
}
