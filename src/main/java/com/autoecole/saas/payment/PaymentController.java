package com.autoecole.saas.payment;

import com.autoecole.saas.payment.dto.PaymentProgressResponse;
import com.autoecole.saas.payment.dto.PaymentRequest;
import com.autoecole.saas.payment.dto.PaymentResponse;
import com.autoecole.saas.payment.dto.PaymentStatsResponse;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponse recordPayment(@RequestBody PaymentRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = currentUser.getId();

        return paymentService.recordPayment(tenantId, userId, request);
    }

    @GetMapping("/progress/{contractId}")
    public PaymentProgressResponse getPaymentProgress(@PathVariable UUID contractId) {
        return paymentService.getPaymentProgress(contractId);
    }

    @GetMapping("/stats")
    public PaymentStatsResponse getPaymentStats() {
        UUID tenantId = TenantContext.getTenantId();
        return paymentService.getPaymentStats(tenantId);
    }
}
