package com.eventlagbe.backend.Controller;

import com.eventlagbe.backend.Models.Participant;
import com.eventlagbe.backend.Models.Organizer;
import com.eventlagbe.backend.Models.Organization;
import com.eventlagbe.backend.Repository.ParticipantRepository;
import com.eventlagbe.backend.Repository.OrganizerRepository;
import com.eventlagbe.backend.Repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@CrossOrigin(origins = "http://localhost:5173")
public class FollowController {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    // Follow a user (supports cross-user-type following)
    @PostMapping("/{currentUserFirebaseUid}/follow/{targetUserFirebaseUid}")
    public ResponseEntity<?> followUser(@PathVariable String currentUserFirebaseUid, @PathVariable String targetUserFirebaseUid) {
        try {
            // Don't allow self-following
            if (currentUserFirebaseUid.equals(targetUserFirebaseUid)) {
                return ResponseEntity.badRequest().body("Cannot follow yourself");
            }

            // Find current user (follower) in any repository
            Object currentUser = findUserByFirebaseUid(currentUserFirebaseUid);
            if (currentUser == null) {
                return ResponseEntity.status(404).body("Current user not found");
            }

            // Find target user in any repository
            Object targetUser = findUserByFirebaseUid(targetUserFirebaseUid);
            if (targetUser == null) {
                return ResponseEntity.status(404).body("Target user not found");
            }

            // Check if already following
            if (isFollowing(currentUser, targetUserFirebaseUid)) {
                return ResponseEntity.badRequest().body("Already following this user");
            }

            // Add to following/followers lists
            addToFollowing(currentUser, targetUserFirebaseUid);
            addToFollowers(targetUser, currentUserFirebaseUid);

            return ResponseEntity.ok().body("Successfully followed user");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error following user: " + e.getMessage());
        }
    }

    // Unfollow a user (supports cross-user-type unfollowing)
    @DeleteMapping("/{currentUserFirebaseUid}/follow/{targetUserFirebaseUid}")
    public ResponseEntity<?> unfollowUser(@PathVariable String currentUserFirebaseUid, @PathVariable String targetUserFirebaseUid) {
        try {
            // Find current user (follower) in any repository
            Object currentUser = findUserByFirebaseUid(currentUserFirebaseUid);
            if (currentUser == null) {
                return ResponseEntity.status(404).body("Current user not found");
            }

            // Find target user in any repository
            Object targetUser = findUserByFirebaseUid(targetUserFirebaseUid);
            if (targetUser == null) {
                return ResponseEntity.status(404).body("Target user not found");
            }

            // Remove from following/followers lists
            removeFromFollowing(currentUser, targetUserFirebaseUid);
            removeFromFollowers(targetUser, currentUserFirebaseUid);

            return ResponseEntity.ok().body("Successfully unfollowed user");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error unfollowing user: " + e.getMessage());
        }
    }

    // Check if current user is following target user
    @GetMapping("/{currentUserFirebaseUid}/is-following/{targetUserFirebaseUid}")
    public ResponseEntity<?> isFollowingUser(@PathVariable String currentUserFirebaseUid, @PathVariable String targetUserFirebaseUid) {
        try {
            Object currentUser = findUserByFirebaseUid(currentUserFirebaseUid);
            if (currentUser == null) {
                return ResponseEntity.status(404).body("Current user not found");
            }

            boolean isFollowing = isFollowing(currentUser, targetUserFirebaseUid);
            return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error checking follow status: " + e.getMessage());
        }
    }

    // Helper method to find user by Firebase UID in any repository
    private Object findUserByFirebaseUid(String firebaseUid) {
        // Check participant repository
        Participant participant = participantRepository.findByFirebaseUid(firebaseUid);
        if (participant != null) {
            return participant;
        }

        // Check organizer repository
        Organizer organizer = organizerRepository.findByFirebaseUid(firebaseUid);
        if (organizer != null) {
            return organizer;
        }

        // Check organization repository
        Organization organization = organizationRepository.findByFirebaseUid(firebaseUid);
        if (organization != null) {
            return organization;
        }

        return null;
    }

    // Helper method to check if user is following target
    private boolean isFollowing(Object user, String targetFirebaseUid) {
        if (user instanceof Participant) {
            Participant p = (Participant) user;
            return p.getFollowing() != null && p.getFollowing().contains(targetFirebaseUid);
        } else if (user instanceof Organizer) {
            Organizer o = (Organizer) user;
            return o.getFollowing() != null && o.getFollowing().contains(targetFirebaseUid);
        } else if (user instanceof Organization) {
            Organization org = (Organization) user;
            return org.getFollowing() != null && org.getFollowing().contains(targetFirebaseUid);
        }
        return false;
    }

    // Helper method to add to following list
    private void addToFollowing(Object user, String targetFirebaseUid) {
        if (user instanceof Participant) {
            Participant p = (Participant) user;
            if (p.getFollowing() == null) {
                p.setFollowing(new ArrayList<>());
            }
            p.getFollowing().add(targetFirebaseUid);
            participantRepository.save(p);
        } else if (user instanceof Organizer) {
            Organizer o = (Organizer) user;
            if (o.getFollowing() == null) {
                o.setFollowing(new ArrayList<>());
            }
            o.getFollowing().add(targetFirebaseUid);
            organizerRepository.save(o);
        } else if (user instanceof Organization) {
            Organization org = (Organization) user;
            if (org.getFollowing() == null) {
                org.setFollowing(new ArrayList<>());
            }
            org.getFollowing().add(targetFirebaseUid);
            organizationRepository.save(org);
        }
    }

    // Helper method to add to followers list
    private void addToFollowers(Object user, String followerFirebaseUid) {
        if (user instanceof Participant) {
            Participant p = (Participant) user;
            if (p.getFollowers() == null) {
                p.setFollowers(new ArrayList<>());
            }
            p.getFollowers().add(followerFirebaseUid);
            participantRepository.save(p);
        } else if (user instanceof Organizer) {
            Organizer o = (Organizer) user;
            if (o.getFollowers() == null) {
                o.setFollowers(new ArrayList<>());
            }
            o.getFollowers().add(followerFirebaseUid);
            organizerRepository.save(o);
        } else if (user instanceof Organization) {
            Organization org = (Organization) user;
            if (org.getFollowers() == null) {
                org.setFollowers(new ArrayList<>());
            }
            org.getFollowers().add(followerFirebaseUid);
            organizationRepository.save(org);
        }
    }

    // Helper method to remove from following list
    private void removeFromFollowing(Object user, String targetFirebaseUid) {
        if (user instanceof Participant) {
            Participant p = (Participant) user;
            if (p.getFollowing() != null) {
                p.getFollowing().remove(targetFirebaseUid);
                participantRepository.save(p);
            }
        } else if (user instanceof Organizer) {
            Organizer o = (Organizer) user;
            if (o.getFollowing() != null) {
                o.getFollowing().remove(targetFirebaseUid);
                organizerRepository.save(o);
            }
        } else if (user instanceof Organization) {
            Organization org = (Organization) user;
            if (org.getFollowing() != null) {
                org.getFollowing().remove(targetFirebaseUid);
                organizationRepository.save(org);
            }
        }
    }

    // Helper method to remove from followers list
    private void removeFromFollowers(Object user, String followerFirebaseUid) {
        if (user instanceof Participant) {
            Participant p = (Participant) user;
            if (p.getFollowers() != null) {
                p.getFollowers().remove(followerFirebaseUid);
                participantRepository.save(p);
            }
        } else if (user instanceof Organizer) {
            Organizer o = (Organizer) user;
            if (o.getFollowers() != null) {
                o.getFollowers().remove(followerFirebaseUid);
                organizerRepository.save(o);
            }
        } else if (user instanceof Organization) {
            Organization org = (Organization) user;
            if (org.getFollowers() != null) {
                org.getFollowers().remove(followerFirebaseUid);
                organizationRepository.save(org);
            }
        }
    }
}
