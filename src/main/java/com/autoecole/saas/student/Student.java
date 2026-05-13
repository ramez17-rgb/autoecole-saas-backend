package com.autoecole.saas.student;
import com.autoecole.saas.monitor.Monitor;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnoreProperties({"students", "tenant"})
    private Monitor mainMonitor;

    @ManyToOne(optional = true)
    @JoinColumn(name = "tenant_id", nullable = true)
    @JsonIgnoreProperties({"students", "users"})
    private Tenant tenant;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"password", "roles", "tenant"})
    private User user;

    private String level;

    @Column(name = "hours_completed")
    private int hoursCompleted = 0;

    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.active;

    @Column(name = "telephone", length = 20)
    private String telephone;

    // ===== Constructors =====
    public Student() {
    }

    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("prenom")
    public String getFirstName() {
        return user != null ? user.getFirstName() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("nom")
    public String getLastName() {
        return user != null ? user.getLastName() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("email")
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("schoolName")
    public String getSchoolName() {
        return tenant != null ? tenant.getName() : null;
    }
}