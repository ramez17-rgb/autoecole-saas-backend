package com.autoecole.saas.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordSetup(String to, String setupUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Set up your AutoEcole account");
            message.setText(
                    "Congratulations! Your school has been approved. Please set up your admin password using the link below:\n\n"
                            + setupUrl + "\n\nThis link will expire in 24 hours.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    public void sendPasswordSetupEmail(String email, String token) {
    }

    public void sendRejectionEmail(String to, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("AutoEcole - Registration Rejected");
            message.setText(
                    "We regret to inform you that your driving school registration has been rejected.\n\n"
                            + "Reason: " + (reason != null ? reason : "No reason provided") + "\n\n"
                            + "Please contact support if you believe this is an error.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send rejection email to " + to + ": " + e.getMessage());
        }
    }
}
