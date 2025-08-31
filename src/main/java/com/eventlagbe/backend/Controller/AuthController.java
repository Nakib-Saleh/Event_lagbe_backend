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

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private OrganizerRepository organizerRepository;
    @Autowired
    private ParticipantRepository participantRepository;

    /* Register API */

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Admin admin) {
        if (admin.getFirebaseUid() == null || admin.getFirebaseUid().isEmpty()) {
            System.out.println("---------");
            System.out.println(admin.getFirebaseUid());
            System.out.println("---------");
            return ResponseEntity.badRequest().body("Missing Firebase UID");
        }
        Admin saved = adminRepository.save(admin);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/register/organization")
    public ResponseEntity<?> registerOrganization(@RequestBody Organization org) {
        if (org.getFirebaseUid() == null || org.getFirebaseUid().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing Firebase UID");
        }
        Organization saved = organizationRepository.save(org);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/register/organizer")
    public ResponseEntity<?> registerOrganizer(@RequestBody Organizer organizer) {
        if (organizer.getFirebaseUid() == null || organizer.getFirebaseUid().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing Firebase UID");
        }
        Organizer saved = organizerRepository.save(organizer);
        // Update the organization to include this organizer's id
        if (saved.getOrganizationId() != null && !saved.getOrganizationId().isEmpty()) {
            Organization org = organizationRepository.findById(saved.getOrganizationId()).orElse(null);
            if (org != null) {
                List<String> organizerIds = org.getOrganizerIds();
                if (organizerIds == null) organizerIds = new java.util.ArrayList<>();
                if (!organizerIds.contains(saved.getId())) {
                    organizerIds.add(saved.getId());
                    org.setOrganizerIds(organizerIds);
                    organizationRepository.save(org);
                }
            }
        }
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/register/participant")
    public ResponseEntity<?> registerParticipant(@RequestBody Participant participant) {
        if (participant.getFirebaseUid() == null || participant.getFirebaseUid().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing Firebase UID");
        }
        Participant saved = participantRepository.save(participant);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{firebaseUid}")
    public ResponseEntity<?> getUserEntity(@PathVariable String firebaseUid) {
        Admin admin = adminRepository.findByFirebaseUid(firebaseUid);
        if (admin != null) return ResponseEntity.ok(java.util.Map.of("role", "admin", "user", admin));
        Organization org = organizationRepository.findByFirebaseUid(firebaseUid);
        if (org != null) return ResponseEntity.ok(java.util.Map.of("role", "organization", "user", org));
        Organizer organizer = organizerRepository.findByFirebaseUid(firebaseUid);
        if (organizer != null) return ResponseEntity.ok(java.util.Map.of("role", "organizer", "user", organizer));
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant != null) return ResponseEntity.ok(java.util.Map.of("role", "participant", "user", participant));
        return ResponseEntity.status(404).body("User not found");
    }

   

    /* Profile API */

    @GetMapping("/admin/{firebaseUid}")
    public ResponseEntity<?> getAdminProfile(@PathVariable String firebaseUid) {
        Admin admin = adminRepository.findByFirebaseUid(firebaseUid);
        if (admin != null) {
            return ResponseEntity.ok(admin);
        }
        return ResponseEntity.status(404).body("Admin not found");
    }

    @GetMapping("/organization/{firebaseUid}")
    public ResponseEntity<?> getOrganizationProfile(@PathVariable String firebaseUid) {
        Organization org = organizationRepository.findByFirebaseUid(firebaseUid);
        if (org != null) {
            return ResponseEntity.ok(org);
        }
        return ResponseEntity.status(404).body("Organization not found");
    }

    @GetMapping("/organizer/{firebaseUid}")
    public ResponseEntity<?> getOrganizerProfile(@PathVariable String firebaseUid) {
        Organizer organizer = organizerRepository.findByFirebaseUid(firebaseUid);
        if (organizer != null) {
            return ResponseEntity.ok(organizer);
        }
        return ResponseEntity.status(404).body("Organizer not found");
    }

    @GetMapping("/participant/{firebaseUid}")
    public ResponseEntity<?> getParticipantProfile(@PathVariable String firebaseUid) {
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant != null) {
            return ResponseEntity.ok(participant);
        }
        return ResponseEntity.status(404).body("Participant not found");
    }

    @PutMapping("/admin/{firebaseUid}")
    public ResponseEntity<?> updateAdminProfile(@PathVariable String firebaseUid, @RequestBody Admin admin) {
        Admin existingAdmin = adminRepository.findByFirebaseUid(firebaseUid);
        if (existingAdmin != null) {
            admin.setId(existingAdmin.getId());
            admin.setFirebaseUid(firebaseUid);
            admin.setCreatedAt(existingAdmin.getCreatedAt());
            admin.setUpdatedAt(java.time.LocalDateTime.now());
            Admin updatedAdmin = adminRepository.save(admin);
            return ResponseEntity.ok(updatedAdmin);
        }
        return ResponseEntity.status(404).body("Admin not found");
    }

    @PutMapping("/organization/{firebaseUid}")
    public ResponseEntity<?> updateOrganizationProfile(@PathVariable String firebaseUid, @RequestBody Organization org) {
        Organization existingOrg = organizationRepository.findByFirebaseUid(firebaseUid);
        if (existingOrg != null) {
            org.setId(existingOrg.getId());
            org.setFirebaseUid(firebaseUid);
            org.setCreatedAt(existingOrg.getCreatedAt());
            org.setUpdatedAt(java.time.LocalDateTime.now());
            Organization updatedOrg = organizationRepository.save(org);
            return ResponseEntity.ok(updatedOrg);
        }
        return ResponseEntity.status(404).body("Organization not found");
    }

    @PutMapping("/organizer/{firebaseUid}")
    public ResponseEntity<?> updateOrganizerProfile(@PathVariable String firebaseUid, @RequestBody Organizer organizer) {
        Organizer existingOrganizer = organizerRepository.findByFirebaseUid(firebaseUid);
        if (existingOrganizer != null) {
            organizer.setId(existingOrganizer.getId());
            organizer.setFirebaseUid(firebaseUid);
            organizer.setCreatedAt(existingOrganizer.getCreatedAt());
            organizer.setUpdatedAt(java.time.LocalDateTime.now());
            Organizer updatedOrganizer = organizerRepository.save(organizer);
            return ResponseEntity.ok(updatedOrganizer);
        }
        return ResponseEntity.status(404).body("Organizer not found");
    }

    @PutMapping("/participant/{firebaseUid}")
    public ResponseEntity<?> updateParticipantProfile(@PathVariable String firebaseUid, @RequestBody Participant participant) {
        Participant existingParticipant = participantRepository.findByFirebaseUid(firebaseUid);
        if (existingParticipant != null) {
            participant.setId(existingParticipant.getId());
            participant.setFirebaseUid(firebaseUid);
            participant.setCreatedAt(existingParticipant.getCreatedAt());
            participant.setUpdatedAt(java.time.LocalDateTime.now());
            Participant updatedParticipant = participantRepository.save(participant);
            return ResponseEntity.ok(updatedParticipant);
        }
        return ResponseEntity.status(404).body("Participant not found");
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        // Check in all user types for username availability
        Admin admin = adminRepository.findByUsername(username);
        if (admin != null) {
            return ResponseEntity.ok(java.util.Map.of("exists", true));
        }
        
        Organization org = organizationRepository.findByUsername(username);
        if (org != null) {
            return ResponseEntity.ok(java.util.Map.of("exists", true));
        }
        
        Organizer organizer = organizerRepository.findByUsername(username);
        if (organizer != null) {
            return ResponseEntity.ok(java.util.Map.of("exists", true));
        }
        
        Participant participant = participantRepository.findByUsername(username);
        if (participant != null) {
            return ResponseEntity.ok(java.util.Map.of("exists", true));
        }
        
        return ResponseEntity.ok(java.util.Map.of("exists", false));
    }
} 