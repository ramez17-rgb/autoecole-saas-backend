package com.autoecole.saas.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, UUID> {
    List<PaymentPlan> findByTenantId(UUID tenantId);
    List<PaymentPlan> findByStudentId(UUID studentId);
}
