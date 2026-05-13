package com.autoecole.saas.payment;

import com.autoecole.saas.payment.dto.ReceiptVerificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;

    @Autowired
    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    public ReceiptVerificationResponse verifyReceipt(UUID verificationCode) {
        Receipt receipt = receiptRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        boolean isAuthentic = verifyHash(receipt);

        return new ReceiptVerificationResponse(
                receipt.getReceiptNumber(),
                receipt.getPayment().getStudent().getUser().getEmail(),
                receipt.getPayment().getTenant().getName(),
                receipt.getPayment().getAmount(),
                receipt.getPayment().getPaymentDate() != null
                        ? receipt.getPayment().getPaymentDate().atStartOfDay()
                        : null,
                isAuthentic);
    }

    public boolean verifyHash(Receipt receipt) {
        String expectedHash = calculateHash(
                receipt.getReceiptNumber(),
                receipt.getPayment().getId(),
                receipt.getPayment().getAmount().toString());
        return expectedHash.equals(receipt.getSecurityHash());
    }

    public String calculateHash(String receiptNumber, UUID paymentId, String amount) {
        try {
            String data = receiptNumber + paymentId.toString() + amount;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculating hash", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
