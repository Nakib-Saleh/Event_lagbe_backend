package com.eventlagbe.backend.Controller;

import com.eventlagbe.backend.Service.ScheduledEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/scheduled-tasks")
@CrossOrigin(origins = "http://localhost:5173")
public class ScheduledTaskController {

    @Autowired
    private ScheduledEventService scheduledEventService;

    /**
     * Manual endpoint to trigger the event deactivation task
     * For testing purposes only
     */
    @PostMapping("/deactivate-expired-events")
    public ResponseEntity<Map<String, Object>> triggerDeactivateExpiredEvents() {
        try {
            scheduledEventService.runDeactivationTaskManually();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Event deactivation task triggered successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Failed to trigger deactivation task: " + e.getMessage()
                ));
        }
    }
}
