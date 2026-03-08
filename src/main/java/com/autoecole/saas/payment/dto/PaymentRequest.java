package com.autoecole.saas.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentRequest {
    private UUID contractId;
    private BigDecimal amount;

    public PaymentRequest() {
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
}
