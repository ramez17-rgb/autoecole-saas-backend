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
    List<Payment> findByContractId(UUID contractId);

    List<Payment> findByTenantId(UUID tenantId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.contract.id = :contractId")
    BigDecimal sumAmountByContractId(@Param("contractId") UUID contractId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.tenant.id = :tenantId")
    long countByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.tenant.id = :tenantId")
    BigDecimal sumAmountByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT p.contract.id FROM Payment p WHERE p.tenant.id = :tenantId GROUP BY p.contract.id HAVING SUM(p.amount) >= (SELECT c.totalPrice FROM StudentContract c WHERE c.id = p.contract.id)")
    List<UUID> findContractsFullyPaidByTenantId(@Param("tenantId") UUID tenantId);
}
