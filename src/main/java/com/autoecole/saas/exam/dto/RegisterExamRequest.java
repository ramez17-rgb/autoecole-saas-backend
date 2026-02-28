package com.autoecole.saas.exam.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public class RegisterExamRequest {
    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotBlank(message = "Exam type is required (code or driving)")
    private String type; // 'code' or 'driving'

    @NotNull(message = "Exam date is required")
    @FutureOrPresent(message = "Exam date must be in the present or future")
    private LocalDate date;

    public RegisterExamRequest() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
