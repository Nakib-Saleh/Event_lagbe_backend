package com.eventlagbe.backend.Repository;

import com.eventlagbe.backend.Models.Organizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrganizerRepository extends MongoRepository<Organizer, String> {
    Organizer findByUsername(String username);
    Organizer findByEmail(String email);
    Organizer findByFirebaseUid(String firebaseUid);
    List<Organizer> findByIsVerified(boolean isVerified);
    List<Organizer> findByOrganizationIdAndIsVerified(String organizationId, boolean isVerified);
    Page<Organizer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String name,
            String email,
            String username,
            Pageable pageable
    );
} 