package com.autoecole.saas.payment;

import com.autoecole.saas.student.Student;
import com.autoecole.saas.tenant.Tenant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A payment plan set by the tenant for a specific student.
 * The student pays in installments against this plan.
 */
@Entity
@Table(name = "payment_plans")
public class PaymentPlan {

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

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("plan")
    private List<PaymentInstallment> installments = new ArrayList<>();

    // Transient computed fields
    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("amountPaid")
    public BigDecimal getAmountPaid() {
        return installments.stream()
                .filter(i -> "APPROVED".equals(i.getStatus()))
                .map(PaymentInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("amountRemaining")
    public BigDecimal getAmountRemaining() {
        return totalAmount.subtract(getAmountPaid());
    }

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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<PaymentInstallment> getInstallments() { return installments; }
    public void setInstallments(List<PaymentInstallment> installments) { this.installments = installments; }
}
