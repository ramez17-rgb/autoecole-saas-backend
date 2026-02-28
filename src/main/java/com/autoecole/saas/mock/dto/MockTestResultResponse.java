package com.autoecole.saas.mock.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class MockTestResultResponse {
    private UUID id;
    private UUID studentId;
    private Integer score;
    private Integer totalQuestions;
    private Boolean passed;
    private LocalDateTime takenAt;

    public MockTestResultResponse() {
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }
}
