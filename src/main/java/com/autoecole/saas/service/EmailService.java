package com.autoecole.saas.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordSetupEmail(String to, String token) {
        // In a real app, this would point to the frontend URL
        // e.g. https://frontend-url/setup-password?token=token
        String subject = "Configuration de votre mot de passe - AutoEcole SaaS";
        String body = "Bienvenue !\n\n" +
                "Votre compte administrateur a été validé.\n" +
                "Veuillez cliquer sur le lien suivant pour configurer votre mot de passe :\n\n" +
                "http://localhost:4200/setup-password?token=" + token + "\n\n" + // Assuming localhost for dev
                "Ce lien est valide pour 24 heures.";

        sendEmail(to, subject, body);
    }

    public void sendRejectionEmail(String to, String reason) {
        String subject = "Votre demande d'inscription - AutoEcole SaaS";
        String body = "Bonjour,\n\n" +
                "Votre demande d'inscription a été refusée pour la raison suivante :\n\n" +
                reason + "\n\n" +
                "Merci de nous contacter pour plus d'informations.";

        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't block flow in this MVP/Demo
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            // In production, handle this properly
        }
    }
}
