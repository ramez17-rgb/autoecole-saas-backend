package com.autoecole.saas.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentProgressResponse {
    private UUID contractId;
    private BigDecimal totalPrice;
    private BigDecimal totalPaid;
    private BigDecimal remainingAmount;
    private double percentagePaid;

    public PaymentProgressResponse(UUID contractId, BigDecimal totalPrice, BigDecimal totalPaid,
            BigDecimal remainingAmount, double percentagePaid) {
        this.contractId = contractId;
        this.totalPrice = totalPrice;
        this.totalPaid = totalPaid;
        this.remainingAmount = remainingAmount;
        this.percentagePaid = percentagePaid;
    }

    public UUID getContractId() {
        return contractId;
    }

    public void setContractId(UUID contractId) {
        this.contractId = contractId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getPercentagePaid() {
        return percentagePaid;
    }

    public void setPercentagePaid(double percentagePaid) {
        this.percentagePaid = percentagePaid;
    }
}
