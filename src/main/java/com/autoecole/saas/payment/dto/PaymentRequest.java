package com.autoecole.saas.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentRequest {
    private UUID studentId;
    private BigDecimal amount;

    public PaymentRequest() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
