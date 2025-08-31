package com.eventlagbe.backend.Controller;

import com.eventlagbe.backend.Models.Event;
import com.eventlagbe.backend.Models.EventTimeslot;
import com.eventlagbe.backend.Models.Organization;
import com.eventlagbe.backend.Models.Organizer;
import com.eventlagbe.backend.Models.Participant;
import com.eventlagbe.backend.Repository.EventRepository;
import com.eventlagbe.backend.Repository.EventTimeslotRepository;
import com.eventlagbe.backend.Repository.OrganizationRepository;
import com.eventlagbe.backend.Repository.OrganizerRepository;
import com.eventlagbe.backend.Repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTimeslotRepository timeslotRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;

    private Instant parseToInstant(Object value) {
        if (value == null) return null;
        String s = value.toString();
        
        // Try parsing as ISO instant first (most common from frontend)
        try {
            return Instant.parse(s);
        } catch (Exception ignored) {
        }
        
        // Try parsing as ISO offset date time
        try {
            return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
        } catch (Exception ignored) {
        }
        
        // Try parsing as ISO local date time and convert to UTC
        try {
            return LocalDateTime.parse(s, ISO).atZone(ZoneOffset.UTC).toInstant();
        } catch (Exception ignored) {
        }
        
        // Try parsing as simple local date time and convert to UTC
        try {
            return LocalDateTime.parse(s).atZone(ZoneOffset.UTC).toInstant();
        } catch (Exception ignored) {
        }
        
        // Try parsing as date only (YYYY-MM-DD format) and convert to UTC
        try {
            if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(s).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
            }
        } catch (Exception ignored) {
        }
        
        System.out.println("Failed to parse date: " + s);
        return null;
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Map<String, Object> payload) {
        // Expected payload keys: ownerId, title, description, location, eventType, eventScope, 
        // requiredSkills[], coHosts[], sponsors[], coverImageUrl, tags[], timeslots[]
        Event event = new Event();
        event.setOwnerId((String) payload.get("ownerId"));
        event.setTitle((String) payload.get("title"));
        event.setDescription((String) payload.get("description"));
        event.setLocation((String) payload.get("location"));
        event.setEventType((String) payload.get("eventType"));
        event.setEventScope((String) payload.get("eventScope"));
        event.setCoverImageUrl((String) payload.get("coverImageUrl"));

        // Handle requiredSkills (skill names)
        if (payload.get("requiredSkills") instanceof List<?> skills) {
            event.setRequiredSkills(skills.stream().map(Object::toString).collect(Collectors.toList()));
        }
        
        // Handle coHosts (IDs)
        if (payload.get("coHosts") instanceof List<?> coHosts) {
            event.setCoHosts(coHosts.stream().map(Object::toString).collect(Collectors.toList()));
        }
        
        // Handle sponsors (names)
        if (payload.get("sponsors") instanceof List<?> sponsors) {
            event.setSponsors(sponsors.stream().map(Object::toString).collect(Collectors.toList()));
        }
        
        if (payload.get("tags") instanceof List<?> tags) {
            event.setTags(tags.stream().map(Object::toString).collect(Collectors.toList()));
        }

        Event saved = eventRepository.save(event);

        // Update creator's eventIds list
        String ownerId = saved.getOwnerId();
        if (ownerId != null) {
            // First try to find as Organization
            Organization organization = organizationRepository.findByFirebaseUid(ownerId);
            if (organization != null) {
                // Update organization's eventIds
                List<String> eventIds = organization.getEventIds();
                if (eventIds == null) {
                    eventIds = new ArrayList<>();
                }
                if (!eventIds.contains(saved.getId())) {
                    eventIds.add(saved.getId());
                    organization.setEventIds(eventIds);
                    organizationRepository.save(organization);
                }
            } else {
                // Try to find as Organizer
                Organizer organizer = organizerRepository.findByFirebaseUid(ownerId);
                if (organizer != null) {
                    // Update organizer's eventIds
                    List<String> eventIds = organizer.getEventIds();
                    if (eventIds == null) {
                        eventIds = new ArrayList<>();
                    }
                    if (!eventIds.contains(saved.getId())) {
                        eventIds.add(saved.getId());
                        organizer.setEventIds(eventIds);
                        organizerRepository.save(organizer);
                    }
                }
            }
        }

        // Save timeslots
        if (payload.get("timeslots") instanceof List<?> timeslots) {
            System.out.println("Received timeslots: " + timeslots);
            System.out.println("Timeslots count: " + timeslots.size());
            
            for (Object obj : timeslots) {
                if (obj instanceof Map<?, ?> m) {
                    EventTimeslot ts = new EventTimeslot();
                    ts.setEventId(saved.getId());
                    Object titleObj = m.get("title");
                    ts.setTitle(titleObj != null ? titleObj.toString() : "Session");
                    Object s = m.get("start");
                    Object e = m.get("end");
                    
                    System.out.println("Processing timeslot: " + ts.getTitle());
                    System.out.println("Raw start: " + s + " (type: " + (s != null ? s.getClass().getSimpleName() : "null") + ")");
                    System.out.println("Raw end: " + e + " (type: " + (e != null ? e.getClass().getSimpleName() : "null") + ")");
                    
                    Instant start = parseToInstant(s);
                    Instant end = parseToInstant(e);
                    
                    System.out.println("Parsed start: " + start);
                    System.out.println("Parsed end: " + end);
                    
                    if (start != null) ts.setStart(start);
                    if (end != null) ts.setEnd(end);
                    
                    // Only save if we have at least a start time
                    if (start != null) {
                        try {
                            timeslotRepository.save(ts);
                            System.out.println("✅ Saved timeslot: " + ts.getTitle() + " from " + start + " to " + end);
                        } catch (Exception ex) {
                            System.err.println("❌ Error saving timeslot: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    } else {
                        System.out.println("❌ Skipping timeslot with null start time: " + ts.getTitle());
                    }
                } else {
                    System.out.println("❌ Invalid timeslot object type: " + (obj != null ? obj.getClass().getSimpleName() : "null"));
                }
            }
        } else {
            System.out.println("No timeslots found in payload or invalid format");
        }

        return ResponseEntity.ok(Map.of("event", saved, "timeslots", timeslotRepository.findByEventId(saved.getId())));
    }

    @GetMapping
    public ResponseEntity<?> listEvents(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.findAll(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable String id) {
        return eventRepository.findById(id)
                .map(e -> ResponseEntity.ok(Map.of(
                        "event", e,
                        "timeslots", timeslotRepository.findByEventId(e.getId())
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{eventId}/bookmark")
    public ResponseEntity<?> toggleBookmark(@PathVariable String eventId, @RequestBody Map<String, String> payload) {
        String participantId = payload.get("participantId");
        if (participantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Participant ID is required"));
        }

        try {
            // Find the participant
            Participant participant = participantRepository.findByFirebaseUid(participantId);
            if (participant == null) {
                return ResponseEntity.notFound().build();
            }

            // Find the event
            Event event = eventRepository.findById(eventId).orElse(null);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }

            List<String> bookmarkedEventIds = participant.getBookmarkedEventIds();
            if (bookmarkedEventIds == null) {
                bookmarkedEventIds = new ArrayList<>();
            }

            List<String> bookmarkedBy = event.getBookmarkedBy();
            if (bookmarkedBy == null) {
                bookmarkedBy = new ArrayList<>();
            }

            boolean isBookmarked = bookmarkedEventIds.contains(eventId);
            
            if (isBookmarked) {
                // Remove bookmark
                bookmarkedEventIds.remove(eventId);
                bookmarkedBy.remove(participantId);
                event.setInterestedCount(Math.max(0, event.getInterestedCount() - 1));
                System.out.println("Removed bookmark: " + participantId + " from event: " + eventId);
                System.out.println("Updated bookmarkedBy list: " + bookmarkedBy);
            } else {
                // Add bookmark
                bookmarkedEventIds.add(eventId);
                bookmarkedBy.add(participantId);
                event.setInterestedCount(event.getInterestedCount() + 1);
                System.out.println("Added bookmark: " + participantId + " to event: " + eventId);
                System.out.println("Updated bookmarkedBy list: " + bookmarkedBy);
            }

            // Save both participant and event
            participant.setBookmarkedEventIds(bookmarkedEventIds);
            event.setBookmarkedBy(bookmarkedBy);
            participantRepository.save(participant);
            eventRepository.save(event);

            return ResponseEntity.ok(Map.of(
                "isBookmarked", !isBookmarked,
                "interestedCount", event.getInterestedCount()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to toggle bookmark"));
        }
    }

    @PostMapping("/{eventId}/going")
    public ResponseEntity<?> toggleGoing(@PathVariable String eventId, @RequestBody Map<String, String> payload) {
        String participantId = payload.get("participantId");
        if (participantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Participant ID is required"));
        }

        try {
            // Find the participant
            Participant participant = participantRepository.findByFirebaseUid(participantId);
            if (participant == null) {
                return ResponseEntity.notFound().build();
            }

            // Find the event
            Event event = eventRepository.findById(eventId).orElse(null);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }

            List<String> registeredEventIds = participant.getRegisteredEventIds();
            if (registeredEventIds == null) {
                registeredEventIds = new ArrayList<>();
            }

            List<String> registeredBy = event.getRegisteredBy();
            if (registeredBy == null) {
                registeredBy = new ArrayList<>();
            }

            boolean isGoing = registeredEventIds.contains(eventId);
            
            if (isGoing) {
                // Remove from going
                registeredEventIds.remove(eventId);
                registeredBy.remove(participantId);
                event.setGoingCount(Math.max(0, event.getGoingCount() - 1));
                System.out.println("Removed from going: " + participantId + " from event: " + eventId);
                System.out.println("Updated registeredBy list: " + registeredBy);
            } else {
                // Add to going
                registeredEventIds.add(eventId);
                registeredBy.add(participantId);
                event.setGoingCount(event.getGoingCount() + 1);
                System.out.println("Added to going: " + participantId + " to event: " + eventId);
                System.out.println("Updated registeredBy list: " + registeredBy);
            }

            // Save both participant and event
            participant.setRegisteredEventIds(registeredEventIds);
            event.setRegisteredBy(registeredBy);
            participantRepository.save(participant);
            eventRepository.save(event);

            return ResponseEntity.ok(Map.of(
                "isGoing", !isGoing,
                "goingCount", event.getGoingCount()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to toggle going status"));
        }
    }

    @GetMapping("/{eventId}/user-status")
    public ResponseEntity<?> getUserStatus(@PathVariable String eventId, @RequestParam String participantId) {
        try {
            // Find the participant
            Participant participant = participantRepository.findByFirebaseUid(participantId);
            if (participant == null) {
                return ResponseEntity.ok(Map.of(
                    "isBookmarked", false,
                    "isGoing", false
                ));
            }

            List<String> bookmarkedEventIds = participant.getBookmarkedEventIds();
            List<String> registeredEventIds = participant.getRegisteredEventIds();

            boolean isBookmarked = bookmarkedEventIds != null && bookmarkedEventIds.contains(eventId);
            boolean isGoing = registeredEventIds != null && registeredEventIds.contains(eventId);

            return ResponseEntity.ok(Map.of(
                "isBookmarked", isBookmarked,
                "isGoing", isGoing
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get user status"));
        }
    }

    @GetMapping("/{eventId}/registered-participants")
    public ResponseEntity<?> getRegisteredParticipants(
            @PathVariable String eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Find the event
            Event event = eventRepository.findById(eventId).orElse(null);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }

            List<String> registeredBy = event.getRegisteredBy();
            if (registeredBy == null || registeredBy.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "participants", new ArrayList<>(),
                    "totalCount", 0,
                    "currentPage", page,
                    "totalPages", 0
                ));
            }

            // Calculate pagination
            int totalCount = registeredBy.size();
            int totalPages = (int) Math.ceil((double) totalCount / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalCount);

            // Get paginated participant IDs
            List<String> paginatedParticipantIds = registeredBy.subList(startIndex, endIndex);

            // Fetch participant details
            List<Map<String, Object>> participants = new ArrayList<>();
            for (String participantId : paginatedParticipantIds) {
                Participant participant = participantRepository.findByFirebaseUid(participantId);
                if (participant != null) {
                    participants.add(Map.of(
                        "firebaseUid", participant.getFirebaseUid(),
                        "name", participant.getName(),
                        "username", participant.getUsername(),
                        "email", participant.getEmail(),
                        "profilePictureUrl", participant.getProfilePictureUrl()
                    ));
                }
            }

            return ResponseEntity.ok(Map.of(
                "participants", participants,
                "totalCount", totalCount,
                "currentPage", page,
                "totalPages", totalPages
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get registered participants"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getEventsByUser(@PathVariable String userId) {
        try {
            // Find events created by the user
            List<Event> events = eventRepository.findByOwnerId(userId);
            
            List<Map<String, Object>> eventList = events.stream()
                .map(event -> {
                    Map<String, Object> eventMap = Map.of(
                        "id", event.getId(),
                        "title", event.getTitle(),
                        "description", event.getDescription(),
                        "location", event.getLocation(),
                        "eventType", event.getEventType(),
                        "isActive", event.getIsActive(),
                        "createdAt", event.getCreatedAt(),
                        "goingCount", event.getGoingCount(),
                        "interestedCount", event.getInterestedCount(),
                        "registeredByCount", event.getRegisteredBy() != null ? event.getRegisteredBy().size() : 0
                    );
                    return eventMap;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("events", eventList));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get user events"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        try {
            Event existingEvent = eventRepository.findById(id).orElse(null);
            if (existingEvent == null) {
                return ResponseEntity.notFound().build();
            }

            // Update event fields
            if (payload.get("title") != null) {
                existingEvent.setTitle((String) payload.get("title"));
            }
            if (payload.get("description") != null) {
                existingEvent.setDescription((String) payload.get("description"));
            }
            if (payload.get("location") != null) {
                existingEvent.setLocation((String) payload.get("location"));
            }
            if (payload.get("eventType") != null) {
                existingEvent.setEventType((String) payload.get("eventType"));
            }
            if (payload.get("eventScope") != null) {
                existingEvent.setEventScope((String) payload.get("eventScope"));
            }
            if (payload.get("coverImageUrl") != null) {
                existingEvent.setCoverImageUrl((String) payload.get("coverImageUrl"));
            }
            if (payload.get("isActive") != null) {
                existingEvent.setIsActive((Boolean) payload.get("isActive"));
            }

            // Handle arrays
            if (payload.get("requiredSkills") instanceof List<?> skills) {
                existingEvent.setRequiredSkills(skills.stream().map(Object::toString).collect(Collectors.toList()));
            }
            if (payload.get("coHosts") instanceof List<?> coHosts) {
                existingEvent.setCoHosts(coHosts.stream().map(Object::toString).collect(Collectors.toList()));
            }
            if (payload.get("sponsors") instanceof List<?> sponsors) {
                existingEvent.setSponsors(sponsors.stream().map(Object::toString).collect(Collectors.toList()));
            }
            if (payload.get("tags") instanceof List<?> tags) {
                existingEvent.setTags(tags.stream().map(Object::toString).collect(Collectors.toList()));
            }

            // Handle timeslots
            if (payload.get("timeslots") instanceof List<?> timeslots) {
                System.out.println("Received timeslots for update: " + timeslots);
                System.out.println("Timeslots count for update: " + timeslots.size());
                
                // Delete existing timeslots
                List<EventTimeslot> existingTimeslots = timeslotRepository.findByEventId(id);
                timeslotRepository.deleteAll(existingTimeslots);
                System.out.println("Deleted " + existingTimeslots.size() + " existing timeslots");

                // Create new timeslots
                List<EventTimeslot> newTimeslots = new ArrayList<>();
                for (Object timeslotObj : timeslots) {
                    if (timeslotObj instanceof Map<?, ?> timeslotMap) {
                        EventTimeslot timeslot = new EventTimeslot();
                        timeslot.setEventId(id);
                        timeslot.setTitle((String) timeslotMap.get("title"));
                        
                        // Parse start time - use 'start' field to match POST endpoint
                        Object startObj = timeslotMap.get("start");
                        if (startObj != null) {
                            Instant startTime = parseToInstant(startObj);
                            timeslot.setStart(startTime);
                            System.out.println("Parsed start time: " + startTime);
                        }
                        
                        // Parse end time - use 'end' field to match POST endpoint
                        Object endObj = timeslotMap.get("end");
                        if (endObj != null) {
                            Instant endTime = parseToInstant(endObj);
                            timeslot.setEnd(endTime);
                            System.out.println("Parsed end time: " + endTime);
                        }
                        
                        newTimeslots.add(timeslot);
                    }
                }
                timeslotRepository.saveAll(newTimeslots);
                System.out.println("Saved " + newTimeslots.size() + " new timeslots");
            } else {
                System.out.println("No timeslots found in update payload");
            }

            existingEvent.setUpdatedAt(Instant.now());
            Event updatedEvent = eventRepository.save(existingEvent);

            return ResponseEntity.ok(updatedEvent);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error updating event: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update event: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id) {
        try {
            Event event = eventRepository.findById(id).orElse(null);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }

            // Remove event from creator's eventIds list
            String ownerId = event.getOwnerId();
            if (ownerId != null) {
                // First try to find as Organization
                Organization organization = organizationRepository.findByFirebaseUid(ownerId);
                if (organization != null) {
                    List<String> eventIds = organization.getEventIds();
                    if (eventIds != null) {
                        eventIds.remove(id);
                        organization.setEventIds(eventIds);
                        organizationRepository.save(organization);
                    }
                } else {
                    // Try to find as Organizer
                    Organizer organizer = organizerRepository.findByFirebaseUid(ownerId);
                    if (organizer != null) {
                        List<String> eventIds = organizer.getEventIds();
                        if (eventIds != null) {
                            eventIds.remove(id);
                            organizer.setEventIds(eventIds);
                            organizerRepository.save(organizer);
                        }
                    }
                }
            }

            // Delete associated timeslots
            List<EventTimeslot> timeslots = timeslotRepository.findByEventId(id);
            timeslotRepository.deleteAll(timeslots);

            // Delete the event
            eventRepository.deleteById(id);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete event"));
        }
    }
}


