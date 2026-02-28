package com.autoecole.saas.stats.dto;

import java.util.Map;
import java.util.UUID;

public class StudentStatisticsResponse {
    private UUID studentId;
    private long totalAttempts;
    private double averageScore;
    private double passRate;
    private Map<String, Long> weakCategories;

    public StudentStatisticsResponse() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public Map<String, Long> getWeakCategories() {
        return weakCategories;
    }

    public void setWeakCategories(Map<String, Long> weakCategories) {
        this.weakCategories = weakCategories;
    }
}
