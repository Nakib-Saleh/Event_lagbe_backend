package com.eventlagbe.backend.Controller;

import com.eventlagbe.backend.Models.Admin;
import com.eventlagbe.backend.Models.Organization;
import com.eventlagbe.backend.Models.Organizer;
import com.eventlagbe.backend.Models.Participant;
import com.eventlagbe.backend.Repository.AdminRepository;
import com.eventlagbe.backend.Repository.OrganizationRepository;
import com.eventlagbe.backend.Repository.OrganizerRepository;
import com.eventlagbe.backend.Repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private OrganizerRepository organizerRepository;
    
    @Autowired
    private ParticipantRepository participantRepository;

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        
        List<Admin> admins = adminRepository.findAll();
        List<Organization> organizations = organizationRepository.findAll();
        List<Organizer> organizers = organizerRepository.findAll();
        List<Participant> participants = participantRepository.findAll();
        
        response.put("admins", admins);
        response.put("organizations", organizations);
        response.put("organizers", organizers);
        response.put("participants", participants);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userType}/{userId}/toggle-verification")
    public ResponseEntity<?> toggleUserVerification(
            @PathVariable String userType,
            @PathVariable String userId) {
        
        System.out.println("Received request to toggle verification for userType: " + userType + ", userId: " + userId);
        
        try {
            switch (userType.toLowerCase()) {
                case "organizations":
                case "organization":
                    Organization org = organizationRepository.findById(userId).orElse(null);
                    if (org != null) {
                        org.setIsVerified(!org.getIsVerified());
                        organizationRepository.save(org);
                        return ResponseEntity.ok(org);
                    }
                    break;
                    
                case "organizers":
                case "organizer":
                    Organizer organizer = organizerRepository.findById(userId).orElse(null);
                    if (organizer != null) {
                        organizer.setIsVerified(!organizer.getIsVerified());
                        organizerRepository.save(organizer);
                        return ResponseEntity.ok(organizer);
                    }
                    break;
                    
                case "participants":
                case "participant":
                    Participant participant = participantRepository.findById(userId).orElse(null);
                    if (participant != null) {
                        participant.setIsVerified(!participant.getIsVerified());
                        participantRepository.save(participant);
                        return ResponseEntity.ok(participant);
                    }
                    break;
                    
                case "admins":
                case "admin":
                    Admin admin = adminRepository.findById(userId).orElse(null);
                    if (admin != null) {
                        // Admins don't have isVerified field, so we return a message
                        return ResponseEntity.ok(Map.of(
                            "message", "Admin verification status cannot be changed",
                            "admin", admin
                        ));
                    }
                    break;
                    
                default:
                    return ResponseEntity.badRequest().body("Invalid user type: " + userType);
            }
            
            System.out.println("User not found for userType: " + userType + ", userId: " + userId);
            return ResponseEntity.status(404).body("User not found for userType: " + userType + ", userId: " + userId);
            
        } catch (Exception e) {
            System.out.println("Error updating verification status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error updating verification status: " + e.getMessage());
        }
    }
}
