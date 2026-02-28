package com.autoecole.saas.stats.dto;

import java.util.Map;
import java.util.UUID;

public class TenantStatisticsResponse {
    private UUID tenantId;
    private double globalPassRate;
    private Map<String, Long> hardestCategories;
    private double averageScorePerMonth;
    // Map of Student UUID -> their Pass Rate or anything else for ranking
    private Map<UUID, Double> studentPerformanceRanking;

    public TenantStatisticsResponse() {
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public double getGlobalPassRate() {
        return globalPassRate;
    }

    public void setGlobalPassRate(double globalPassRate) {
        this.globalPassRate = globalPassRate;
    }

    public Map<String, Long> getHardestCategories() {
        return hardestCategories;
    }

    public void setHardestCategories(Map<String, Long> hardestCategories) {
        this.hardestCategories = hardestCategories;
    }

    public double getAverageScorePerMonth() {
        return averageScorePerMonth;
    }

    public void setAverageScorePerMonth(double averageScorePerMonth) {
        this.averageScorePerMonth = averageScorePerMonth;
    }

    public Map<UUID, Double> getStudentPerformanceRanking() {
        return studentPerformanceRanking;
    }

    public void setStudentPerformanceRanking(Map<UUID, Double> studentPerformanceRanking) {
        this.studentPerformanceRanking = studentPerformanceRanking;
    }
}
