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

    }
}
