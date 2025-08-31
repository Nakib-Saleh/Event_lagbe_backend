package com.eventlagbe.backend.Controller;

import com.eventlagbe.backend.Models.Participant;
import com.eventlagbe.backend.Models.Event;
import com.eventlagbe.backend.Models.Organization;
import com.eventlagbe.backend.Repository.ParticipantRepository;
import com.eventlagbe.backend.Repository.EventRepository;
import com.eventlagbe.backend.Repository.OrganizationRepository;
import com.eventlagbe.backend.Service.FirebaseService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/participant")
@CrossOrigin(origins = "http://localhost:5173")
public class ParticipantController {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private FirebaseService firebaseService;

    @GetMapping
    public ResponseEntity<List<Participant>> getAllParticipants() {
        List<Participant> allParticipants = participantRepository.findAll();
        return ResponseEntity.ok(allParticipants);
    }

    @GetMapping("/unverified")
    public ResponseEntity<List<Participant>> getUnverifiedParticipants() {
        List<Participant> unverifiedParticipants = participantRepository.findByIsVerified(false);
        return ResponseEntity.ok(unverifiedParticipants);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Participant> approveParticipant(@PathVariable String id) {
        Participant participant = participantRepository.findById(id).orElse(null);
        if (participant != null) {
            participant.setIsVerified(true);
            participantRepository.save(participant);
            return ResponseEntity.ok(participant);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/reject")
    public ResponseEntity<Void> rejectParticipant(@PathVariable String id) {
        Participant participant = participantRepository.findById(id).orElse(null);
        if (participant != null) {
            try {
                String firebaseUid = participant.getFirebaseUid();
                if (firebaseUid != null && !firebaseUid.isEmpty()) {
                    firebaseService.deleteUser(firebaseUid);
                }
                participantRepository.delete(participant);
                return ResponseEntity.ok().build();
            } catch (FirebaseAuthException e) {
                return ResponseEntity.internalServerError().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    // Dashboard endpoints for participants
    @GetMapping("/{firebaseUid}/bookmarked-events")
    public ResponseEntity<List<Event>> getBookmarkedEvents(@PathVariable String firebaseUid) {
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Event> bookmarkedEvents = new ArrayList<>();
        if (participant.getBookmarkedEventIds() != null) {
            for (String eventId : participant.getBookmarkedEventIds()) {
                Event event = eventRepository.findById(eventId).orElse(null);
                if (event != null) {
                    // Fetch organizer information
                    if (event.getOwnerId() != null) {
                        Organization organizer = organizationRepository.findById(event.getOwnerId()).orElse(null);
                        if (organizer != null) {
                            event.setOrganizerName(organizer.getName());
                        }
                    }
                    
                    bookmarkedEvents.add(event);
                }
            }
        }
        
        return ResponseEntity.ok(bookmarkedEvents);
    }

    @GetMapping("/{firebaseUid}/registered-events")
    public ResponseEntity<List<Event>> getRegisteredEvents(@PathVariable String firebaseUid) {
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Event> registeredEvents = new ArrayList<>();
        if (participant.getRegisteredEventIds() != null) {
            for (String eventId : participant.getRegisteredEventIds()) {
                Event event = eventRepository.findById(eventId).orElse(null);
                if (event != null) {
                    // Fetch organizer information
                    if (event.getOwnerId() != null) {
                        Organization organizer = organizationRepository.findById(event.getOwnerId()).orElse(null);
                        if (organizer != null) {
                            event.setOrganizerName(organizer.getName());
                        }
                    }
                    
                    registeredEvents.add(event);
                }
            }
        }
        
        return ResponseEntity.ok(registeredEvents);
    }



    @GetMapping("/{firebaseUid}/followers")
    public ResponseEntity<List<Participant>> getFollowers(@PathVariable String firebaseUid) {
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Participant> followers = new ArrayList<>();
        if (participant.getFollowers() != null) {
            for (String followerId : participant.getFollowers()) {
                Participant follower = participantRepository.findById(followerId).orElse(null);
                if (follower != null) {
                    followers.add(follower);
                }
            }
        }
        
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{firebaseUid}/following")
    public ResponseEntity<List<Participant>> getFollowing(@PathVariable String firebaseUid) {
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Participant> following = new ArrayList<>();
        if (participant.getFollowing() != null) {
            for (String followingId : participant.getFollowing()) {
                Participant followedUser = participantRepository.findById(followingId).orElse(null);
                if (followedUser != null) {
                    following.add(followedUser);
                }
            }
        }
        
        return ResponseEntity.ok(following);
    }

    @DeleteMapping("/{firebaseUid}/bookmark/{eventId}")
    public ResponseEntity<?> removeBookmark(@PathVariable String firebaseUid, @PathVariable String eventId) {
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (participant.getBookmarkedEventIds() != null) {
            participant.getBookmarkedEventIds().remove(eventId);
            participantRepository.save(participant);
        }
        
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{firebaseUid}/register/{eventId}")
    public ResponseEntity<?> unregisterFromEvent(@PathVariable String firebaseUid, @PathVariable String eventId) {
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (participant.getRegisteredEventIds() != null) {
            participant.getRegisteredEventIds().remove(eventId);
            participantRepository.save(participant);
        }
        
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{firebaseUid}/followers/{followerId}")
    public ResponseEntity<?> removeFollower(@PathVariable String firebaseUid, @PathVariable String followerId) {
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (participant.getFollowers() != null) {
            participant.getFollowers().remove(followerId);
            participantRepository.save(participant);
        }
        
        return ResponseEntity.ok().build();
    }

        @DeleteMapping("/{firebaseUid}/following/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable String firebaseUid, @PathVariable String userId) {
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }

        if (participant.getFollowing() != null) {
            participant.getFollowing().remove(userId);
            participantRepository.save(participant);
        }

        return ResponseEntity.ok().build();
    }


} 