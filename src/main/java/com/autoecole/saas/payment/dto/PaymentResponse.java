package com.autoecole.saas.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentResponse {
    private UUID id;
    private UUID contractId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String receiptPdfUrl;

    public PaymentResponse(UUID id, UUID contractId, BigDecimal amount, LocalDateTime paymentDate,
            String receiptPdfUrl) {
        this.id = id;
        this.contractId = contractId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.receiptPdfUrl = receiptPdfUrl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getContractId() {
        return contractId;
    }

    public void setContractId(UUID contractId) {
        this.contractId = contractId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getReceiptPdfUrl() {
        return receiptPdfUrl;
    }

    public void setReceiptPdfUrl(String receiptPdfUrl) {
        this.receiptPdfUrl = receiptPdfUrl;
    }
}
