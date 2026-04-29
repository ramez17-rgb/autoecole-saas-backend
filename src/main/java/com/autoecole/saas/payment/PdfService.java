package com.autoecole.saas.payment;

import com.autoecole.saas.student.Student;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class PdfService {

    public String generateReceiptPdf(Receipt receipt, Student student, BigDecimal totalPaid,
            byte[] qrCodeImageBytes) {
        try {
            String directoryPath = "receipts/" + student.getTenant().getId().toString() + "/";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = receipt.getReceiptNumber() + ".pdf";
            String filePath = directoryPath + fileName;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));

            document.open();

            // Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("PAYMENT RECEIPT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Driving School Info
            String schoolName = student.getTenant().getName();
            Paragraph schoolInfo = new Paragraph("Driving School: " + schoolName);
            schoolInfo.setSpacingAfter(20);
            document.add(schoolInfo);

            // Receipt Info Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addCellToTable(table, "Receipt Number:", receipt.getReceiptNumber());
            addCellToTable(table, "Date:",
                    receipt.getPayment().getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            addCellToTable(table, "Student Email:", student.getUser().getEmail());
            addCellToTable(table, "Amount Paid:", receipt.getPayment().getAmount().toString() + " TND");
            addCellToTable(table, "Total Validated Paid So Far:", totalPaid.toString() + " TND");

            document.add(table);

            // QR Code
            if (qrCodeImageBytes != null) {
                Image qrImage = Image.getInstance(qrCodeImageBytes);
                qrImage.scaleAbsolute(100, 100);
                qrImage.setAlignment(Element.ALIGN_CENTER);
                document.add(qrImage);
            }

            // Admin Stamp
            if (student.getTenant().getStampImage() != null && !student.getTenant().getStampImage().isEmpty()) {
                try {
                    String base64Image = student.getTenant().getStampImage().split(",")[1];
                    byte[] stampBytes = Base64.getDecoder().decode(base64Image);
                    Image stampImage = Image.getInstance(stampBytes);
                    stampImage.scaleAbsolute(100, 100);
                    stampImage.setAlignment(Element.ALIGN_RIGHT);
                    document.add(stampImage);
                } catch (Exception e) {
                    System.err.println("Could not load stamp image: " + e.getMessage());
                }
            }

            document.close();

            return filePath;
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error generating PDF receipt", e);
        }
    }

    private void addCellToTable(PdfPTable table, String label, String value) {
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }
}
