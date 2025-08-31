package com.eventlagbe.backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmailToParticipants(String subject, String message, List<String> participantEmails, String fromEmail) {
        int successCount = 0;
        int failureCount = 0;
        
        System.out.println("Starting to send emails to " + participantEmails.size() + " participants");
        System.out.println("From email: " + fromEmail);
        System.out.println("Subject: " + subject);
        
        for (String email : participantEmails) {
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(email);
                mailMessage.setSubject(subject);
                mailMessage.setText(message);
                mailMessage.setFrom("eventlagbe@gmail.com"); // Must match authenticated account
                
                // Add Reply-To header to show organization's email
                if (!fromEmail.equals("eventlagbe@gmail.com")) {
                    mailMessage.setReplyTo(fromEmail);
                }
                
                mailSender.send(mailMessage);
                successCount++;
                System.out.println("Successfully sent email to: " + email + " with Reply-To: " + fromEmail);
            } catch (Exception e) {
                failureCount++;
                System.err.println("Failed to send email to " + email + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Email sending completed. Success: " + successCount + ", Failed: " + failureCount);
    }

    // Overloaded method for backward compatibility
    public void sendEmailToParticipants(String subject, String message, List<String> participantEmails) {
        sendEmailToParticipants(subject, message, participantEmails, "eventlagbe@gmail.com");
    }

    public void sendEmailToSingleParticipant(String to, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailMessage.setFrom("eventlagbe@gmail.com");
            
            mailSender.send(mailMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}
