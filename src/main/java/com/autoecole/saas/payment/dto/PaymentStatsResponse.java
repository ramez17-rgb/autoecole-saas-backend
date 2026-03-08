package com.autoecole.saas.payment.dto;

import java.math.BigDecimal;

public class PaymentStatsResponse {
    private BigDecimal totalRevenue;
    private long numberOfPayments;
    private BigDecimal averagePayment;
    private int studentsFullyPaid;

    public PaymentStatsResponse(BigDecimal totalRevenue, long numberOfPayments, BigDecimal averagePayment,
            int studentsFullyPaid) {
        this.totalRevenue = totalRevenue;
        this.numberOfPayments = numberOfPayments;
        this.averagePayment = averagePayment;
        this.studentsFullyPaid = studentsFullyPaid;
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

    public int getStudentsFullyPaid() {
        return studentsFullyPaid;
    }

    public void setStudentsFullyPaid(int studentsFullyPaid) {
        this.studentsFullyPaid = studentsFullyPaid;
    }
}
