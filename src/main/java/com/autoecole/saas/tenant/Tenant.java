package com.autoecole.saas.tenant;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(nullable = false)
    private String name;

    @Column(name = "status", nullable = false)
    private String status = "active";

    // Address and contact_email are in schema but seemingly optional or missing in
    // original java
    // I will leave them out for now as they are nullable in schema or not strictly
    // required if not constrained.
    // Schema: address TEXT, contact_email VARCHAR(150), status ...
    // Note: Schema says address, contact_email are nullable.

    // getters setters

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setActive(boolean active) {
        this.status = active ? "active" : "suspended";
    }
}