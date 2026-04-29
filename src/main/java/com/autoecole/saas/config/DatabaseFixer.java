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
            System.out.println("Applying database fix: Dropping NOT NULL constraint on students.tenant_id");
            jdbcTemplate.execute("ALTER TABLE students ALTER COLUMN tenant_id DROP NOT NULL");
            System.out.println("Database fix (students.tenant_id) applied successfully.");
        } catch (Exception e) {
            System.out.println("Database fix (students.tenant_id) failed: " + e.getMessage());
        }

        // Fix telephone column type: Hibernate ddl-auto=update does NOT change existing column types.
        // The column may have been created as INT; it must be VARCHAR for phone numbers.
        try {
            System.out.println("Applying database fix: Ensuring users.telephone is VARCHAR(20)");
            jdbcTemplate.execute(
                "ALTER TABLE users ALTER COLUMN telephone TYPE VARCHAR(20) USING telephone::VARCHAR");
            System.out.println("Database fix (users.telephone to VARCHAR) applied successfully.");
        } catch (Exception e) {
            System.out.println("Database fix (users.telephone) skipped or failed: " + e.getMessage());
        }

        try {
            System.out.println("Applying database fix: Ensuring students.telephone exists (VARCHAR(20))");
            jdbcTemplate.execute(
                    "DO $$ BEGIN ALTER TABLE students ADD COLUMN telephone VARCHAR(20); "
                            + "EXCEPTION WHEN duplicate_column THEN NULL; END $$");
            System.out.println("Database fix (students.telephone column) applied successfully.");
        } catch (Exception e) {
            System.out.println("Database fix (students.telephone add) skipped or failed: " + e.getMessage());
        }
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE students ALTER COLUMN telephone TYPE VARCHAR(20) USING telephone::text");
            System.out.println("Database fix (students.telephone to VARCHAR) applied successfully.");
        } catch (Exception e) {
            System.out.println("Database fix (students.telephone type) skipped or failed: " + e.getMessage());
        }

        try {
            System.out.println("Applying database fix: Ensuring monitors.telephone exists (VARCHAR(20))");
            jdbcTemplate.execute(
                    "DO $$ BEGIN ALTER TABLE monitors ADD COLUMN telephone VARCHAR(20); "
                            + "EXCEPTION WHEN duplicate_column THEN NULL; END $$");
            System.out.println("Database fix (monitors.telephone column) applied successfully.");
        } catch (Exception e) {
            System.out.println("Database fix (monitors.telephone add) skipped or failed: " + e.getMessage());
        }
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE monitors ALTER COLUMN telephone TYPE VARCHAR(20) USING telephone::text");
            System.out.println("Database fix (monitors.telephone to VARCHAR) applied successfully.");
        } catch (Exception e) {
            System.out.println("Database fix (monitors.telephone type) skipped or failed: " + e.getMessage());
        }
    }

    }

