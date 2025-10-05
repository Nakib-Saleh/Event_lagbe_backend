package com.eventlagbe.backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String mailUsername;
    
    @Value("${spring.mail.host:}")
    private String mailHost;

    public void sendEmailToParticipants(String subject, String message, List<String> participantEmails, String fromEmail) {
        
        if (mailUsername == null || mailUsername.isEmpty() || mailHost == null || mailHost.isEmpty()) {
            System.err.println("Email configuration is not properly set. Skipping email sending.");
            System.err.println("MAIL_USERNAME: " + (mailUsername != null && !mailUsername.isEmpty() ? "SET" : "NOT SET"));
            System.err.println("MAIL_HOST: " + (mailHost != null && !mailHost.isEmpty() ? "SET" : "NOT SET"));
            return;
        }
        
        int successCount = 0;
        int failureCount = 0;
        
        System.out.println("Starting to send emails to " + participantEmails.size() + " participants");
        System.out.println("From email: " + fromEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Using SMTP host: " + mailHost);
        
        for (String email : participantEmails) {
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(email);
                mailMessage.setSubject(subject);
                mailMessage.setText(message);
                mailMessage.setFrom(mailUsername); 
                
                
                if (!fromEmail.equals(mailUsername)) {
                    mailMessage.setReplyTo(fromEmail);
                }
                
                mailSender.send(mailMessage);
                successCount++;
                System.out.println("Successfully sent email to: " + email + " with Reply-To: " + fromEmail);
            } catch (Exception e) {
                failureCount++;
                System.err.println("Failed to send email to " + email + ": " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Root cause: " + e.getCause().getMessage());
                }
                
                if (!e.getMessage().contains("timeout") && !e.getMessage().contains("Connect timed out")) {
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("Email sending completed. Success: " + successCount + ", Failed: " + failureCount);
    }

    
    public void sendEmailToParticipants(String subject, String message, List<String> participantEmails) {
        sendEmailToParticipants(subject, message, participantEmails, mailUsername != null ? mailUsername : "eventlagbe@gmail.com");
    }

    public void sendEmailToSingleParticipant(String to, String subject, String message) {
        
        if (mailUsername == null || mailUsername.isEmpty() || mailHost == null || mailHost.isEmpty()) {
            System.err.println("Email configuration is not properly set. Cannot send email to " + to);
            throw new RuntimeException("Email configuration is not properly set");
        }
        
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailMessage.setFrom(mailUsername);
            
            mailSender.send(mailMessage);
            System.out.println("Successfully sent email to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}
