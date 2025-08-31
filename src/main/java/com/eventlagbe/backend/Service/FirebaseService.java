package com.eventlagbe.backend.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FirebaseService {
    
    private FirebaseAuth firebaseAuth;
    
    @Value("${firebase.service-account.path:classpath:firebase-service-account.json}")
    private String serviceAccountPath;
    
    @PostConstruct
    public void initializeFirebase() {
        try {
            // Initialize Firebase Admin SDK
            FirebaseOptions options;
            
            // Check if we're in Railway (environment variable exists)
            String firebaseServiceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
            
            if (firebaseServiceAccountJson != null && !firebaseServiceAccountJson.trim().isEmpty()) {
                // Create service account file from environment variable
                Path tempFile = createServiceAccountFile(firebaseServiceAccountJson);
                GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(tempFile));
                options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
            } else {
                // Try to load from service account file if available
                InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");
                if (serviceAccount != null) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                    options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();
                } else {
                    // Fallback to default credentials
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .build();
                }
            }
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            firebaseAuth = FirebaseAuth.getInstance();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
    
    private Path createServiceAccountFile(String jsonContent) throws IOException {
        // Create temp file
        Path tempFile = Files.createTempFile("firebase-service-account", ".json");
        
        // Write JSON content to temp file
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
            writer.write(jsonContent);
        }
        
        return tempFile;
    }
    
    public void deleteUser(String firebaseUid) throws FirebaseAuthException {
        if (firebaseUid != null && !firebaseUid.isEmpty()) {
            firebaseAuth.deleteUser(firebaseUid);
        }
    }
} 