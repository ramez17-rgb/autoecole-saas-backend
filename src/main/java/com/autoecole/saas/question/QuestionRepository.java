package com.autoecole.saas.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByTenantId(UUID tenantId);

    @Query(value = "SELECT * FROM questions q WHERE q.tenant_id = :tenantId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestionsByTenant(@Param("tenantId") UUID tenantId, @Param("limit") int limit);
}
