package com.eventlagbe.backend.Repository;

import com.eventlagbe.backend.Models.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrganizationRepository extends MongoRepository<Organization, String> {
    Organization findByUsername(String username);
    Organization findByEmail(String email);
    Organization findByFirebaseUid(String firebaseUid);
    List<Organization> findByIsVerified(boolean isVerified);
    Page<Organization> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String name,
            String email,
            String username,
            Pageable pageable
    );
} 