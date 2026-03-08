package com.autoecole.saas.payment;

import com.autoecole.saas.payment.dto.ReceiptVerificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    @Autowired
    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping("/verify/{verificationCode}")
    public ReceiptVerificationResponse verifyReceipt(@PathVariable UUID verificationCode) {
        return receiptService.verifyReceipt(verificationCode);
    }
}
