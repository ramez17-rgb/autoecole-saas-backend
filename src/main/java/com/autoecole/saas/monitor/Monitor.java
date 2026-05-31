package com.autoecole.saas.monitor;

import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.user.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "monitors")
public class Monitor {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "telephone", length = 20)
    private String telephone;

    public Monitor() {
    }

    // getters & setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("firstName")
    public String getFirstName() {
        return user != null ? user.getFirstName() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("lastName")
    public String getLastName() {
        return user != null ? user.getLastName() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("email")
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }
}
