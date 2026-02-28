package com.autoecole.saas.stats;

import com.autoecole.saas.stats.dto.StudentStatisticsResponse;
import com.autoecole.saas.stats.dto.TenantStatisticsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentStatisticsResponse> getStudentStats(@PathVariable UUID studentId) {
        StudentStatisticsResponse response = statisticsService.getStudentStatistics(studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenant")
    public ResponseEntity<TenantStatisticsResponse> getTenantStats() {
        TenantStatisticsResponse response = statisticsService.getTenantStatistics();
        return ResponseEntity.ok(response);
    }
}
