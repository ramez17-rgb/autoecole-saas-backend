package com.autoecole.saas.payment;

import com.autoecole.saas.audit.AuditService;
import com.autoecole.saas.payment.dto.PaymentProgressResponse;
import com.autoecole.saas.payment.dto.PaymentRequest;
import com.autoecole.saas.payment.dto.PaymentResponse;
import com.autoecole.saas.payment.dto.PaymentStatsResponse;
import com.autoecole.saas.student.StudentContract;
import com.autoecole.saas.student.StudentContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StudentContractRepository contractRepository;
    private final ReceiptRepository receiptRepository;
    private final AuditService auditService;
    private final QrCodeService qrCodeService;
    private final PdfService pdfService;
    private final ReceiptService receiptService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
            StudentContractRepository contractRepository,
            ReceiptRepository receiptRepository,
            AuditService auditService,
            QrCodeService qrCodeService,
            PdfService pdfService,
            ReceiptService receiptService) {
        this.paymentRepository = paymentRepository;
        this.contractRepository = contractRepository;
        this.receiptRepository = receiptRepository;
        this.auditService = auditService;
        this.qrCodeService = qrCodeService;
        this.pdfService = pdfService;
        this.receiptService = receiptService;
    }

    @Transactional
    public PaymentResponse recordPayment(UUID tenantId, UUID userId, PaymentRequest request) {
        StudentContract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Contract does not belong to the tenant");
        }

        // Save Payment
        Payment payment = new Payment();
        payment.setTenant(contract.getTenant());
        payment.setStudent(contract.getStudent());
        payment.setContract(contract);
        payment.setAmount(request.getAmount());
        Payment savedPayment = paymentRepository.save(payment);

        auditService.log(tenantId, userId, "PAYMENT_CREATED");

        // Generate Receipt
        Receipt receipt = new Receipt();
        receipt.setPayment(savedPayment);
        receipt.setReceiptNumber(generateReceiptNumber());
        receipt.setVerificationCode(UUID.randomUUID());
        receipt.setSecurityHash(receiptService.calculateHash(
                receipt.getReceiptNumber(),
                savedPayment.getId(),
                savedPayment.getAmount().toString()));

        // Calculate Total Paid so far for PDF
        BigDecimal totalPaid = paymentRepository.sumAmountByContractId(contract.getId());

        // Generate QR Code
        String verificationUrl = baseUrl + "/api/receipts/verify/" + receipt.getVerificationCode().toString();
        byte[] qrCodeBytes = qrCodeService.generateQrCode(verificationUrl, 200, 200);

        // Generate PDF
        String pdfPath = pdfService.generateReceiptPdf(receipt, contract, totalPaid, qrCodeBytes);
        receipt.setPdfPath(pdfPath);

        receiptRepository.save(receipt);

        auditService.log(tenantId, userId, "RECEIPT_GENERATED");

        return new PaymentResponse(
                savedPayment.getId(),
                contract.getId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentDate(),
                pdfPath);
    }

    public PaymentProgressResponse getPaymentProgress(UUID contractId) {
        StudentContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        BigDecimal totalPaid = paymentRepository.sumAmountByContractId(contractId);
        BigDecimal remainingAmount = contract.getTotalPrice().subtract(totalPaid);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        double percentagePaid = 0.0;
        if (contract.getTotalPrice().compareTo(BigDecimal.ZERO) > 0) {
            percentagePaid = totalPaid.divide(contract.getTotalPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }

        return new PaymentProgressResponse(
                contract.getId(),
                contract.getTotalPrice(),
                totalPaid,
                remainingAmount,
                percentagePaid);
    }

    public PaymentStatsResponse getPaymentStats(UUID tenantId) {
        BigDecimal totalRevenue = paymentRepository.sumAmountByTenantId(tenantId);
        long numberOfPayments = paymentRepository.countByTenantId(tenantId);

        BigDecimal averagePayment = BigDecimal.ZERO;
        if (numberOfPayments > 0) {
            averagePayment = totalRevenue.divide(new BigDecimal(numberOfPayments), 2, RoundingMode.HALF_UP);
        }

        List<UUID> fullyPaidContracts = paymentRepository.findContractsFullyPaidByTenantId(tenantId);
        int studentsFullyPaid = fullyPaidContracts.size();

        return new PaymentStatsResponse(
                totalRevenue,
                numberOfPayments,
                averagePayment,
                studentsFullyPaid);
    }

    private String generateReceiptNumber() {
        int year = Year.now().getValue();
        long count = receiptRepository.count() + 1;
        return String.format("REC-%d-%04d", year, count);
    }
}
