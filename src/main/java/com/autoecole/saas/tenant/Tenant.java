package com.autoecole.saas.tenant;

import jakarta.persistence.*;
import java.time.LocalDateTime;
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
    private String status = "PENDING"; // Default to PENDING

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "refusal_reason")
    private String refusalReason;

    // Contact info (optional as per schema discussions)
    private String address;
    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "stamp_image", columnDefinition = "TEXT")
    private String stampImage;

    public Tenant() {
    }

    public Tenant(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public String getRefusalReason() {
        return refusalReason;
    }

    public void setRefusalReason(String refusalReason) {
        this.refusalReason = refusalReason;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public String getStampImage() {
        return stampImage;
    }

    public void setStampImage(String stampImage) {
        this.stampImage = stampImage;
    }
}
