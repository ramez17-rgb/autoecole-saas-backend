package com.autoecole.saas.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single installment payment against a PaymentPlan.
 * When saved, a Receipt is automatically generated.
 */
@Entity
@Table(name = "payment_installments")
public class PaymentInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PaymentPlan plan;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private String mode = "cash";

    @Column(name = "receipt_number", unique = true)
    private String receiptNumber;

    @Column(name = "verification_code", unique = true)
    private UUID verificationCode;

    @Column(nullable = true)
    private String status = "PENDING";

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("planStudentName")
    public String getPlanStudentName() {
        if (plan == null || plan.getStudent() == null) return null;
        return plan.getStudentName();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public PaymentPlan getPlan() { return plan; }
    public void setPlan(PaymentPlan plan) { this.plan = plan; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public UUID getVerificationCode() { return verificationCode; }
    public void setVerificationCode(UUID verificationCode) { this.verificationCode = verificationCode; }

    public String getStatus() { return status != null ? status : "PENDING"; }
    public void setStatus(String status) { this.status = status; }
}
