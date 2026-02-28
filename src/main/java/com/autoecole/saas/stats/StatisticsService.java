package com.autoecole.saas.stats;

import com.autoecole.saas.mock.MockTest;
import com.autoecole.saas.mock.MockTestRepository;
import com.autoecole.saas.stats.dto.StudentStatisticsResponse;
import com.autoecole.saas.stats.dto.TenantStatisticsResponse;
import com.autoecole.saas.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final MockTestRepository mockTestRepository;
    private final EntityManager entityManager;

    public StatisticsService(MockTestRepository mockTestRepository, EntityManager entityManager) {
        this.mockTestRepository = mockTestRepository;
        this.entityManager = entityManager;
    }

    public StudentStatisticsResponse getStudentStatistics(UUID studentId) {
        UUID tenantId = TenantContext.getTenantId();

        List<MockTest> mockTests = mockTestRepository.findByTenantIdAndStudentId(tenantId, studentId);

        long totalAttempts = mockTests.size();
        double passRate = totalAttempts == 0 ? 0
                : ((double) mockTests.stream().filter(MockTest::getPassed).count() / totalAttempts) * 100;
        double averageScore = totalAttempts == 0 ? 0
                : mockTests.stream().mapToInt(m -> m.getScore() != null ? m.getScore() : 0).average().orElse(0.0);

        // Weak categories using Native Query with EntityManager to easily return list
        // of objects
        String sql = "SELECT q.category, COUNT(mta.id) as wrong_count " +
                "FROM mock_test_answers mta " +
                "JOIN mock_tests mt ON mta.mock_test_id = mt.id " +
                "JOIN questions q ON mta.question_id = q.id " +
                "WHERE mt.student_id = :studentId AND mta.is_correct = false " +
                "GROUP BY q.category " +
                "ORDER BY wrong_count DESC";

        List<Object[]> weakCategoriesResult = entityManager.createNativeQuery(sql)
                .setParameter("studentId", studentId)
                .getResultList();

        Map<String, Long> weakCategories = new HashMap<>();
        for (Object[] row : weakCategoriesResult) {
            weakCategories.put((String) row[0], ((Number) row[1]).longValue());
        }

        StudentStatisticsResponse response = new StudentStatisticsResponse();
        response.setStudentId(studentId);
        response.setTotalAttempts(totalAttempts);
        response.setAverageScore(averageScore);
        response.setPassRate(passRate);
        response.setWeakCategories(weakCategories);

        return response;
    }

    public TenantStatisticsResponse getTenantStatistics() {
        UUID tenantId = TenantContext.getTenantId();

        List<MockTest> mockTests = mockTestRepository.findByTenantId(tenantId);

        long totalAttempts = mockTests.size();
        double globalPassRate = totalAttempts == 0 ? 0
                : ((double) mockTests.stream().filter(MockTest::getPassed).count() / totalAttempts) * 100;

        double averageScore = totalAttempts == 0 ? 0
                : mockTests.stream().mapToInt(m -> m.getScore() != null ? m.getScore() : 0).average().orElse(0.0);

        // Hardest categories for the whole tenant
        String sql = "SELECT q.category, COUNT(mta.id) as wrong_count " +
                "FROM mock_test_answers mta " +
                "JOIN mock_tests mt ON mta.mock_test_id = mt.id " +
                "JOIN questions q ON mta.question_id = q.id " +
                "WHERE mt.tenant_id = :tenantId AND mta.is_correct = false " +
                "GROUP BY q.category " +
                "ORDER BY wrong_count DESC";

        List<Object[]> hardestCategoriesResult = entityManager.createNativeQuery(sql)
                .setParameter("tenantId", tenantId)
                .getResultList();

        Map<String, Long> hardestCategories = new HashMap<>();
        for (Object[] row : hardestCategoriesResult) {
            hardestCategories.put((String) row[0], ((Number) row[1]).longValue());
        }

        // Student performance ranking map (simplified average score map)
        Map<UUID, Double> ranking = mockTests.stream()
                .collect(Collectors.groupingBy(m -> m.getStudent().getId(),
                        Collectors.averagingInt(m -> m.getScore() != null ? m.getScore() : 0)));

        TenantStatisticsResponse response = new TenantStatisticsResponse();
        response.setTenantId(tenantId);
        response.setGlobalPassRate(globalPassRate);
        response.setHardestCategories(hardestCategories);
        response.setAverageScorePerMonth(averageScore); // Approximated over all tests currently
        response.setStudentPerformanceRanking(ranking);

        return response;
    }
}
