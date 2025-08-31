package com.eventlagbe.backend.Repository;

import com.eventlagbe.backend.Models.Participant;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ParticipantRepository extends MongoRepository<Participant, String> {
    Participant findByUsername(String username);
    Participant findByEmail(String email);
    Participant findByFirebaseUid(String firebaseUid);
    java.util.List<Participant> findByIsVerified(boolean isVerified);
} 