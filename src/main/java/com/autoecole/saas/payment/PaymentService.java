package com.autoecole.saas.payment;

import com.autoecole.saas.audit.AuditService;
import com.autoecole.saas.payment.dto.PaymentProgressResponse;
import com.autoecole.saas.payment.dto.PaymentRequest;
import com.autoecole.saas.payment.dto.PaymentResponse;
import com.autoecole.saas.payment.dto.PaymentStatsResponse;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
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
    private final StudentRepository studentRepository;
    private final ReceiptRepository receiptRepository;
    private final AuditService auditService;
    private final QrCodeService qrCodeService;
    private final PdfService pdfService;
    private final ReceiptService receiptService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
            StudentRepository studentRepository,
            ReceiptRepository receiptRepository,
            AuditService auditService,
            QrCodeService qrCodeService,
            PdfService pdfService,
            ReceiptService receiptService) {
        this.paymentRepository = paymentRepository;
        this.studentRepository = studentRepository;
        this.receiptRepository = receiptRepository;
        this.auditService = auditService;
        this.qrCodeService = qrCodeService;
        this.pdfService = pdfService;
        this.receiptService = receiptService;
    }

    @Transactional
    public PaymentResponse recordPayment(UUID tenantId, UUID userId, PaymentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Student does not belong to the tenant");
        }

        // Save Payment
        Payment payment = new Payment();
        payment.setTenant(student.getTenant());
        payment.setStudent(student);
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
        BigDecimal totalPaid = paymentRepository.sumAmountByStudentId(student.getId());

        // Generate QR Code
        String verificationUrl = baseUrl + "/api/receipts/verify/" + receipt.getVerificationCode().toString();
        byte[] qrCodeBytes = qrCodeService.generateQrCode(verificationUrl, 200, 200);

        // Generate PDF
        String pdfPath = pdfService.generateReceiptPdf(receipt, student, totalPaid, qrCodeBytes);
        receipt.setPdfPath(pdfPath);

        receiptRepository.save(receipt);

        auditService.log(tenantId, userId, "RECEIPT_GENERATED");

        return new PaymentResponse(
                savedPayment.getId(),
                student.getId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentDate(),
                pdfPath);
    }

    public PaymentProgressResponse getPaymentProgress(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        BigDecimal totalPaid = paymentRepository.sumAmountByStudentId(studentId);

        return new PaymentProgressResponse(
                student.getId(),
                totalPaid);
    }

    public PaymentStatsResponse getPaymentStats(UUID tenantId) {
        BigDecimal totalRevenue = paymentRepository.sumAmountByTenantId(tenantId);
        long numberOfPayments = paymentRepository.countByTenantId(tenantId);

        BigDecimal averagePayment = BigDecimal.ZERO;
        if (numberOfPayments > 0) {
            averagePayment = totalRevenue.divide(new BigDecimal(numberOfPayments), 2, RoundingMode.HALF_UP);
        }

        return new PaymentStatsResponse(
                totalRevenue,
                numberOfPayments,
                averagePayment);
    }

    public List<java.util.Map<String, Object>> getContracts(UUID tenantId) {
        return contractRepository.findByTenantId(tenantId).stream().map(c -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", c.getId());
            map.put("totalPrice", c.getTotalPrice());
            String studentName = "";
            if (c.getStudent() != null && c.getStudent().getUser() != null) {
                studentName = c.getStudent().getUser().getEmail();
            }
            map.put("studentName", studentName);
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    private String generateReceiptNumber() {
        int year = Year.now().getValue();
        long count = receiptRepository.count() + 1;
        return String.format("REC-%d-%04d", year, count);
    }
}
