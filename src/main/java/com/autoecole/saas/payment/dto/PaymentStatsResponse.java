package com.autoecole.saas.payment.dto;

import java.math.BigDecimal;

public class PaymentStatsResponse {
    private BigDecimal totalRevenue;
    private long numberOfPayments;
    private BigDecimal averagePayment;

    public PaymentStatsResponse(BigDecimal totalRevenue, long numberOfPayments, BigDecimal averagePayment) {
        this.totalRevenue = totalRevenue;
        this.numberOfPayments = numberOfPayments;
        this.averagePayment = averagePayment;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getNumberOfPayments() {
        return numberOfPayments;
    }

    public void setNumberOfPayments(long numberOfPayments) {
        this.numberOfPayments = numberOfPayments;
    }

    public BigDecimal getAveragePayment() {
        return averagePayment;
    }

    public void setAveragePayment(BigDecimal averagePayment) {
        this.averagePayment = averagePayment;
    }
}
