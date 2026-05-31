package com.autoecole.saas.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPaymentStats() {
        return ResponseEntity.ok(paymentService.getPaymentStats());
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody CreatePaymentRequest request) {
        Payment payment = paymentService.createPayment(
                request.studentId(),
                request.amount(),
                request.mode()
        );
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    public record CreatePaymentRequest(UUID studentId, BigDecimal amount, String mode) {}
}

// Student-facing payment endpoint
@RestController
@RequestMapping("/api/student/payments")
class StudentPaymentController {

    private final PaymentService paymentService;

    public StudentPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getStudentPayments(@RequestParam UUID studentId) {
        return ResponseEntity.ok(paymentService.getPaymentsByStudent(studentId));
    }
}
