package com.eventlagbe.backend.Controller;
import com.eventlagbe.backend.Models.Organizer;
import com.eventlagbe.backend.Repository.OrganizerRepository;
import com.eventlagbe.backend.Service.FirebaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/organizer")
@CrossOrigin(origins = "http://localhost:5173")
public class OrganizerController {

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private OrganizerRepository organizerRepository;

    @GetMapping("/unverified")
    public ResponseEntity<List<Organizer>> getUnverifiedOrganizers() {
        List<Organizer> unverifiedOrganizers = organizerRepository.findByIsVerified(false);
        return ResponseEntity.ok(unverifiedOrganizers);
    }

    @GetMapping
    public ResponseEntity<?> getAllOrganizers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        if (q != null && !q.isBlank()) {
            Page<Organizer> result = organizerRepository
                    .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(q, q, q, pageable);
            return ResponseEntity.ok(result);
        }
        Page<Organizer> all = organizerRepository.findAll(pageable);
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{organizationId}/unverified-organizers")
    public ResponseEntity<List<?>> getUnverifiedOrganizersByOrganization(@PathVariable String organizationId) {
        return ResponseEntity.ok(organizerRepository.findByOrganizationIdAndIsVerified(organizationId, false));
    }

    @GetMapping("/{organizationId}/verified-organizers")
    public ResponseEntity<List<Organizer>> getVerifiedOrganizersByOrganization(@PathVariable String organizationId) {
        return ResponseEntity.ok(organizerRepository.findByOrganizationIdAndIsVerified(organizationId, true));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Organizer> approveOrganizer(@PathVariable String id) {
        Organizer organizer = organizerRepository.findById(id).orElse(null);
        if (organizer != null) {
            organizer.setIsVerified(true);
            organizerRepository.save(organizer);
            return ResponseEntity.ok(organizer);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/reject")
    public ResponseEntity<Void> rejectOrganizer(@PathVariable String id) {
        Organizer organizer = organizerRepository.findById(id).orElse(null);
        if (organizer != null) {
            try {
                // Delete from Firebase first
                String firebaseUid = organizer.getFirebaseUid();
                if (firebaseUid != null && !firebaseUid.isEmpty()) {
                    firebaseService.deleteUser(firebaseUid);
                }
                
                // Then delete from MongoDB
                organizerRepository.delete(organizer);
                return ResponseEntity.ok().build();
            } catch (FirebaseAuthException e) {
                // If Firebase deletion fails, donot delete from MongoDB
                return ResponseEntity.internalServerError().build();
            }
        }
        return ResponseEntity.notFound().build();
    }


} 