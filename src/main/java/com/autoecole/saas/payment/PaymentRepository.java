package com.autoecole.saas.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByStudentId(UUID studentId);

    List<Payment> findByTenantId(UUID tenantId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.student.id = :studentId")
    BigDecimal sumAmountByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.tenant.id = :tenantId")
    long countByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.tenant.id = :tenantId")
    BigDecimal sumAmountByTenantId(@Param("tenantId") UUID tenantId);
}
