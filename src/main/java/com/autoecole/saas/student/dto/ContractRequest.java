package com.autoecole.saas.student.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ContractRequest {
    private UUID studentId;
    private BigDecimal totalPrice;

    public ContractRequest() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
