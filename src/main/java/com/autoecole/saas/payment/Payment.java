package com.autoecole.saas.payment;

import com.autoecole.saas.student.Student;
import com.autoecole.saas.tenant.Tenant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    @JsonIgnoreProperties({"students", "users"})
    private Tenant tenant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"sessions", "tenant", "mainMonitor"})
    private Student student;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private String mode = "cash";

    // Constructors
    public Payment() {}

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    // Transient helper for student name in JSON
    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("studentName")
    public String getStudentName() {
        if (student == null) return null;
        String first = student.getFirstName();
        String last = student.getLastName();
        if (first != null || last != null) {
            return ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
        }
        return student.getEmail();
    }
}
