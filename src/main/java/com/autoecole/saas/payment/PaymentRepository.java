package com.autoecole.saas.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByTenantId(UUID tenantId);
    List<Payment> findByStudentId(UUID studentId);
    List<Payment> findByTenantIdOrderByPaymentDateDesc(UUID tenantId);
}
