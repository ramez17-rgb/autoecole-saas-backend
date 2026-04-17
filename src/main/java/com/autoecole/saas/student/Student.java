package com.autoecole.saas.student;

import com.autoecole.saas.monitor.Monitor;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue
    private UUID id;

    // ✅ NEW FIELDS (FIX)
    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "telephone")
    private Integer telephone;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sessions"})
    @ManyToOne(optional = true)
    @JoinColumn(name = "main_monitor_id")
    private Monitor mainMonitor;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles", "tenant"})
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String level;

    @Column(name = "hours_completed")
    private int hoursCompleted = 0;

    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.active;

    // ===== Constructors =====
    public Student() {
    }

    @JsonIgnore
    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
    private List<com.autoecole.saas.session.Session> sessions = new ArrayList<>();

    // ===== Getters & Setters =====

    public UUID getId() {
        return id;
    }

    // ✅ NEW GETTERS
    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getHoursCompleted() {
        return hoursCompleted;
    }

    public void setHoursCompleted(int hoursCompleted) {
        this.hoursCompleted = hoursCompleted;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public Monitor getMainMonitor() {
        return mainMonitor;
    }

    public void setMainMonitor(Monitor mainMonitor) {
        this.mainMonitor = mainMonitor;
    }

    public List<com.autoecole.saas.session.Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<com.autoecole.saas.session.Session> sessions) {
        this.sessions = sessions;
    }
    public Integer getTelephone() {
        return telephone;
    }

    public void setTelephone(Integer telephone) {
        this.telephone = telephone;
    }
}