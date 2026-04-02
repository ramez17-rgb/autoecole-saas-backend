package com.autoecole.saas.student;
import com.autoecole.saas.monitor.Monitor;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.user.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne(optional = true)
    @JoinColumn(name = "main_monitor_id")
    private Monitor mainMonitor;

    @ManyToOne(optional = true)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String nom;
    private String prenom;

    private String level;

    @Column(name = "hours_completed")
    private int hoursCompleted = 0;

    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.active;

    // ===== Constructors =====
    public Student() {
    }

    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
    private java.util.List<com.autoecole.saas.session.Session> sessions = new java.util.ArrayList<>();

    public java.util.List<com.autoecole.saas.session.Session> getSessions() {
        return sessions;
    }

    public void setSessions(java.util.List<com.autoecole.saas.session.Session> sessions) {
        this.sessions = sessions;
    }

    // ===== Getters & Setters =====
    public UUID getId() {
        return id;
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
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
}