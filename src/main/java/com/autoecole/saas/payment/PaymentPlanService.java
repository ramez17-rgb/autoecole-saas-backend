package com.autoecole.saas.payment;

import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.tenant.TenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentPlanService {

    private final PaymentPlanRepository planRepository;
    private final PaymentInstallmentRepository installmentRepository;
    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;

    public PaymentPlanService(PaymentPlanRepository planRepository,
                              PaymentInstallmentRepository installmentRepository,
                              StudentRepository studentRepository,
                              TenantRepository tenantRepository) {
        this.planRepository = planRepository;
        this.installmentRepository = installmentRepository;
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public PaymentPlan createPlan(UUID studentId, BigDecimal totalAmount, String description) {
        UUID tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        PaymentPlan plan = new PaymentPlan();
        plan.setTenant(tenant);
        plan.setStudent(student);
        plan.setTotalAmount(totalAmount);
        plan.setDescription(description);
        return planRepository.save(plan);
    }

    public List<PaymentPlan> getPlansForTenant() {
        UUID tenantId = TenantContext.getTenantId();
        return planRepository.findByTenantId(tenantId);
    }

    public List<PaymentPlan> getPlansForStudent(UUID studentId) {
        return planRepository.findByStudentId(studentId);
    }

    @Transactional
    public PaymentInstallment recordInstallment(UUID planId, BigDecimal amount, String mode) {
        return createInstallment(planId, amount, mode, "APPROVED");
    }

    @Transactional
    public PaymentInstallment recordStudentInstallment(UUID planId, BigDecimal amount, String mode) {
        return createInstallment(planId, amount, mode, "PENDING");
    }

    private PaymentInstallment createInstallment(UUID planId, BigDecimal amount, String mode, String status) {
        PaymentPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Payment plan not found"));

        PaymentInstallment installment = new PaymentInstallment();
        installment.setPlan(plan);
        installment.setAmount(amount);
        installment.setPaymentDate(LocalDate.now());
        installment.setMode(mode != null ? mode : "cash");
        installment.setStatus(status);

        // Auto-generate receipt number
        String receiptNumber = generateReceiptNumber(plan);
        installment.setReceiptNumber(receiptNumber);
        installment.setVerificationCode(UUID.randomUUID());

        return installmentRepository.save(installment);
    }

    @Transactional
    public PaymentInstallment approveInstallment(UUID installmentId) {
        PaymentInstallment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found"));
        installment.setStatus("APPROVED");
        return installmentRepository.save(installment);
    }

    public List<PaymentInstallment> getInstallmentsForPlan(UUID planId) {
        return installmentRepository.findByPlanId(planId);
    }

    public List<PaymentInstallment> getInstallmentsForStudent(UUID studentId) {
        return installmentRepository.findByPlanStudentId(studentId);
    }

    private String generateReceiptNumber(PaymentPlan plan) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = installmentRepository.findByPlanId(plan.getId()).size() + 1;
        String shortId = plan.getId().toString().replace("-", "").substring(0, 6).toUpperCase();
        return String.format("REC-%s-%s-%03d", date, shortId, count);
    }

    public java.util.Map<String, Object> getTenantStats() {
        UUID tenantId = TenantContext.getTenantId();
        List<PaymentPlan> plans = planRepository.findByTenantId(tenantId);
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long numberOfPayments = 0;
        long studentsFullyPaid = 0;

        for (PaymentPlan plan : plans) {
            BigDecimal paid = plan.getAmountPaid();
            totalRevenue = totalRevenue.add(paid);
            if (plan.getTotalAmount().compareTo(BigDecimal.ZERO) > 0 && plan.getAmountRemaining().compareTo(BigDecimal.ZERO) <= 0) {
                studentsFullyPaid++;
            }
            numberOfPayments += plan.getInstallments().stream()
                    .filter(i -> "APPROVED".equals(i.getStatus()))
                    .count();
        }

        double average = numberOfPayments > 0 ? totalRevenue.doubleValue() / numberOfPayments : 0;

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("numberOfPayments", numberOfPayments);
        stats.put("averagePayment", average);
        stats.put("studentsFullyPaid", studentsFullyPaid);
        return stats;
    }
}
