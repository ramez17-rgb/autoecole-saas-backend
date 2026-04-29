package com.autoecole.saas.payment;

import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.tenant.TenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;

    public PaymentService(PaymentRepository paymentRepository, StudentRepository studentRepository, TenantRepository tenantRepository) {
        this.paymentRepository = paymentRepository;
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<Payment> getAllPayments() {
        UUID tenantId = TenantContext.getTenantId();
        return paymentRepository.findByTenantIdOrderByPaymentDateDesc(tenantId);
    }

    public List<Payment> getPaymentsByStudent(UUID studentId) {
        return paymentRepository.findByStudentId(studentId);
    }

    public Map<String, Object> getPaymentStats() {
        UUID tenantId = TenantContext.getTenantId();
        List<Payment> payments = paymentRepository.findByTenantId(tenantId);

        BigDecimal totalRevenue = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int numberOfPayments = payments.size();

        BigDecimal averagePayment = numberOfPayments > 0
                ? totalRevenue.divide(BigDecimal.valueOf(numberOfPayments), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("numberOfPayments", numberOfPayments);
        stats.put("averagePayment", averagePayment);
        stats.put("studentsFullyPaid", 0); // Can be enhanced later

        return stats;
    }

    @Transactional
    public Payment createPayment(UUID studentId, BigDecimal amount, String mode) {
        UUID tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Payment payment = new Payment();
        payment.setTenant(tenant);
        payment.setStudent(student);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDate.now());
        payment.setMode(mode != null ? mode : "cash");

        return paymentRepository.save(payment);
    }
}
