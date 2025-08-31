package com.eventlagbe.backend.Controller;

import com.eventlagbe.backend.Service.EmailService;
import com.eventlagbe.backend.Repository.EventRepository;
import com.eventlagbe.backend.Repository.OrganizerRepository;
import com.eventlagbe.backend.Repository.OrganizationRepository;
import com.eventlagbe.backend.Models.Event;
import com.eventlagbe.backend.Models.Organizer;
import com.eventlagbe.backend.Models.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EmailController {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private OrganizerRepository organizerRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;

    @PostMapping("/{eventId}/send-mail")
    public ResponseEntity<Map<String, Object>> sendMailToParticipants(
            @PathVariable String eventId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String subject = (String) requestBody.get("subject");
            String message = (String) requestBody.get("message");
            String senderEmail = (String) requestBody.get("senderEmail");
            @SuppressWarnings("unchecked")
            List<String> participantEmails = (List<String>) requestBody.get("participantEmails");

            if (subject == null || message == null || participantEmails == null || participantEmails.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Missing required fields"));
            }

            // Determine sender email for Reply-To header
            String replyToEmail = "eventlagbe@gmail.com"; // Default fallback
            
            if (senderEmail != null) {
                // Use provided sender email
                replyToEmail = senderEmail;
            } else {
                // Try to get sender email from event creator
                try {
                    Optional<Event> eventOpt = eventRepository.findById(eventId);
                    if (eventOpt.isPresent()) {
                        Event event = eventOpt.get();
                        String creatorId = event.getOwnerId();
                        
                        // Try to find organizer first
                        Organizer organizer = organizerRepository.findByFirebaseUid(creatorId);
                        if (organizer != null) {
                            replyToEmail = organizer.getEmail();
                        } else {
                            // Try to find organization
                            Organization org = organizationRepository.findByFirebaseUid(creatorId);
                            if (org != null) {
                                replyToEmail = org.getEmail();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching sender email: " + e.getMessage());
                }
            }

            emailService.sendEmailToParticipants(subject, message, participantEmails, replyToEmail);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Emails sent successfully to " + participantEmails.size() + " participants"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Failed to send emails: " + e.getMessage()));
        }
    }
}
