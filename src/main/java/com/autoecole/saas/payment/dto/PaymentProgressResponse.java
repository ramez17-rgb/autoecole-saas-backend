package com.autoecole.saas.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentProgressResponse {
    private UUID studentId;
    private BigDecimal totalPaid;

    public PaymentProgressResponse(UUID studentId, BigDecimal totalPaid) {
        this.studentId = studentId;
        this.totalPaid = totalPaid;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }
}
