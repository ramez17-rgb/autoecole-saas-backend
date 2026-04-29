package com.autoecole.saas.payment;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "receipt_number", nullable = false, unique = true)
    private String receiptNumber;

    @Column(name = "verification_code", nullable = false, unique = true)
    private UUID verificationCode;

    @Column(name = "security_hash", columnDefinition = "TEXT")
    private String securityHash;

    @Column(name = "pdf_path")
    private String pdfPath;

    public Receipt() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public UUID getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(UUID verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getSecurityHash() {
        return securityHash;
    }

    public void setSecurityHash(String securityHash) {
        this.securityHash = securityHash;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }
}
