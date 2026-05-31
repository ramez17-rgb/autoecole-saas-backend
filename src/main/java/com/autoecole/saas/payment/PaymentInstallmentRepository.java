package com.autoecole.saas.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PaymentInstallmentRepository extends JpaRepository<PaymentInstallment, UUID> {
    List<PaymentInstallment> findByPlanId(UUID planId);
    List<PaymentInstallment> findByPlanStudentId(UUID studentId);
}
