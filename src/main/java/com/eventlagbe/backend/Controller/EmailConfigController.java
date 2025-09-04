package com.eventlagbe.backend.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "http://localhost:5173")
public class EmailConfigController {

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:}")
    private String mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @GetMapping("/config-status")
    public ResponseEntity<Map<String, Object>> getEmailConfigStatus() {
        Map<String, Object> response = new HashMap<>();
        
        boolean isConfigured = mailHost != null && !mailHost.isEmpty() && 
                              mailUsername != null && !mailUsername.isEmpty() && 
                              mailPassword != null && !mailPassword.isEmpty();
        
        response.put("isConfigured", isConfigured);
        response.put("host", mailHost != null && !mailHost.isEmpty() ? "SET" : "NOT SET");
        response.put("port", mailPort != null && !mailPort.isEmpty() ? mailPort : "NOT SET");
        response.put("username", mailUsername != null && !mailUsername.isEmpty() ? "SET" : "NOT SET");
        response.put("password", mailPassword != null && !mailPassword.isEmpty() ? "SET" : "NOT SET");
        
        if (!isConfigured) {
            response.put("message", "Email configuration is incomplete. Please set MAIL_HOST, MAIL_USERNAME, and MAIL_PASSWORD environment variables.");
        } else {
            response.put("message", "Email configuration appears to be set correctly.");
        }
        
        return ResponseEntity.ok(response);
    }
}
