package com.autoecole.saas.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Tenant-facing payment plan controller.
 * Base path: /api/admin/payment-plans
 */
@RestController
@RequestMapping("/api/admin/payment-plans")
public class PaymentPlanController {

    private final PaymentPlanService planService;

    public PaymentPlanController(PaymentPlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<PaymentPlan> createPlan(@RequestBody CreatePlanRequest request) {
        PaymentPlan plan = planService.createPlan(request.getStudentId(), request.getTotalAmount(), request.getDescription());
        return new ResponseEntity<>(plan, HttpStatus.CREATED);
    }

    /** Get all plans for the current tenant */
    @GetMapping
    public ResponseEntity<List<PaymentPlan>> getPlans() {
        return ResponseEntity.ok(planService.getPlansForTenant());
    }

    /** Get plans for a specific student */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PaymentPlan>> getPlansForStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(planService.getPlansForStudent(studentId));
    }

    @PostMapping("/{planId}/installments")
    public ResponseEntity<PaymentInstallment> recordInstallment(
            @PathVariable UUID planId,
            @RequestBody RecordInstallmentRequest request) {
        PaymentInstallment inst = planService.recordInstallment(planId, request.getAmount(), request.getMode());
        return new ResponseEntity<>(inst, HttpStatus.CREATED);
    }

    /** Get all installments for a plan */
    @GetMapping("/{planId}/installments")
    public ResponseEntity<List<PaymentInstallment>> getInstallments(@PathVariable UUID planId) {
        return ResponseEntity.ok(planService.getInstallmentsForPlan(planId));
    }

    /** Approve an installment */
    @PutMapping("/installments/{installmentId}/approve")
    public ResponseEntity<PaymentInstallment> approveInstallment(@PathVariable UUID installmentId) {
        return ResponseEntity.ok(planService.approveInstallment(installmentId));
    }

    /** Get stats for the tenant's payment plans */
    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getStats() {
        return ResponseEntity.ok(planService.getTenantStats());
    }

    public static class CreatePlanRequest {
        private UUID studentId;
        private BigDecimal totalAmount;
        private String description;

        public CreatePlanRequest() {}

        public UUID getStudentId() { return studentId; }
        public void setStudentId(UUID studentId) { this.studentId = studentId; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class RecordInstallmentRequest {
        private BigDecimal amount;
        private String mode;

        public RecordInstallmentRequest() {}

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
    }
}
