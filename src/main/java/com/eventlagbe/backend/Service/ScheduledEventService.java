package com.eventlagbe.backend.Service;

import com.eventlagbe.backend.Models.Event;
import com.eventlagbe.backend.Models.EventTimeslot;
import com.eventlagbe.backend.Repository.EventRepository;
import com.eventlagbe.backend.Repository.EventTimeslotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ScheduledEventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTimeslotRepository eventTimeslotRepository;

    /**
     * Scheduled task to deactivate events when all their timeslots have ended
     * Runs every day at 00:00:00 (midnight) Bangladesh time
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Dhaka")
    public void deactivateExpiredEvents() {
        System.out.println("🕐 Starting scheduled task: deactivateExpiredEvents at " + 
                          LocalDateTime.now(ZoneId.of("Asia/Dhaka")));
        
        try {
            // 1. Fetch all active events
            List<Event> activeEvents = eventRepository.findByIsActiveTrue();
            System.out.println("📋 Found " + activeEvents.size() + " active events to check");
            
            int deactivatedCount = 0;
            
            // 2. For each active event
            for (Event event : activeEvents) {
                boolean shouldDeactivate = checkIfEventShouldBeDeactivated(event);
                
                if (shouldDeactivate) {
                    // 3. Deactivate the event
                    event.setIsActive(false);
                    eventRepository.save(event);
                    deactivatedCount++;
                    
                    System.out.println("❌ Deactivated event: " + event.getTitle() + " (ID: " + event.getId() + ")");
                } else {
                    System.out.println("✅ Event still active: " + event.getTitle() + " (ID: " + event.getId() + ")");
                }
            }
            
            System.out.println("🎯 Scheduled task completed. Deactivated " + deactivatedCount + " events out of " + activeEvents.size() + " checked");
            
        } catch (Exception e) {
            System.err.println("❌ Error in scheduled task deactivateExpiredEvents: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if an event should be deactivated based on its timeslots
     * @param event The event to check
     * @return true if event should be deactivated, false otherwise
     */
    private boolean checkIfEventShouldBeDeactivated(Event event) {
        try {
            // Get all timeslots for this event
            List<EventTimeslot> timeslots = eventTimeslotRepository.findByEventId(event.getId());
            
            if (timeslots.isEmpty()) {
                System.out.println("⚠️ Event " + event.getTitle() + " has no timeslots - keeping active");
                return false;
            }
            
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Dhaka"));
            boolean allTimeslotsExpired = true;
            
            // Check each timeslot
            for (EventTimeslot timeslot : timeslots) {
                Instant endTime = timeslot.getEnd();
                
                if (endTime == null) {
                    System.out.println("⚠️ Timeslot " + timeslot.getId() + " has no end time - keeping event active");
                    return false;
                }
                
                // Convert Instant to LocalDateTime for comparison
                LocalDateTime endDateTime = endTime.atZone(ZoneId.of("Asia/Dhaka")).toLocalDateTime();
                
                // If any timeslot hasn't ended yet, keep the event active
                if (endDateTime.isAfter(now)) {
                    System.out.println("⏰ Timeslot " + timeslot.getId() + " ends at " + endDateTime + " (future) - keeping event active");
                    allTimeslotsExpired = false;
                    break;
                } else {
                    System.out.println("⏰ Timeslot " + timeslot.getId() + " ended at " + endDateTime + " (past)");
                }
            }
            
            return allTimeslotsExpired;
            
        } catch (Exception e) {
            System.err.println("❌ Error checking timeslots for event " + event.getId() + ": " + e.getMessage());
            // In case of error, don't deactivate the event
            return false;
        }
    }

    /**
     * Manual method to run the deactivation task (for testing)
     */
    public void runDeactivationTaskManually() {
        System.out.println("🔧 Running deactivation task manually...");
        deactivateExpiredEvents();
    }
}
