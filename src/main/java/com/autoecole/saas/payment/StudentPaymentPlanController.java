package com.autoecole.saas.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Student-facing payment plan controller.
 * Base path: /api/student/payment-plans
 */
@RestController
@RequestMapping("/api/student/payment-plans")
public class StudentPaymentPlanController {

    private final PaymentPlanService planService;

    public StudentPaymentPlanController(PaymentPlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<PaymentPlan>> getMyPlans(@RequestParam UUID studentId) {
        return ResponseEntity.ok(planService.getPlansForStudent(studentId));
    }

    @GetMapping("/my/installments")
    public ResponseEntity<List<PaymentInstallment>> getMyInstallments(@RequestParam UUID studentId) {
        return ResponseEntity.ok(planService.getInstallmentsForStudent(studentId));
    }

    @PostMapping("/{planId}/installments")
    public ResponseEntity<PaymentInstallment> recordMyInstallment(
            @PathVariable UUID planId,
            @RequestBody PaymentPlanController.RecordInstallmentRequest request) {
        PaymentInstallment inst = planService.recordStudentInstallment(planId, request.getAmount(), request.getMode());
        return new ResponseEntity<>(inst, HttpStatus.CREATED);
    }
}
