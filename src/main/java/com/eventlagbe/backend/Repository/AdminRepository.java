package com.eventlagbe.backend.Repository;

import com.eventlagbe.backend.Models.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminRepository extends MongoRepository<Admin, String> {
    Admin findByUsername(String username);
    Admin findByEmail(String email);
    Admin findByFirebaseUid(String firebaseUid);
} 