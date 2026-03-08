package com.autoecole.saas.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReceiptVerificationResponse {
    private String receiptNumber;
    private String studentName;
    private String drivingSchoolName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private boolean isAuthentic;

    public ReceiptVerificationResponse(String receiptNumber, String studentName, String drivingSchoolName,
            BigDecimal amount, LocalDateTime paymentDate, boolean isAuthentic) {
        this.receiptNumber = receiptNumber;
        this.studentName = studentName;
        this.drivingSchoolName = drivingSchoolName;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.isAuthentic = isAuthentic;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDrivingSchoolName() {
        return drivingSchoolName;
    }

    public void setDrivingSchoolName(String drivingSchoolName) {
        this.drivingSchoolName = drivingSchoolName;
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

    public boolean isAuthentic() {
        return isAuthentic;
    }

    public void setAuthentic(boolean authentic) {
        isAuthentic = authentic;
    }
}
