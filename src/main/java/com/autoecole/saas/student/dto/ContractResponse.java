package com.autoecole.saas.student.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ContractResponse {
    private UUID id;
    private UUID studentId;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;

    public ContractResponse(UUID id, UUID studentId, BigDecimal totalPrice, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
