package com.eventlagbe.backend.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseService {
    
    private FirebaseAuth firebaseAuth;
    
    @PostConstruct
    public void initializeFirebase() {
        try {
            // Initialize Firebase Admin SDK
            // Try to use service account file first, fallback to default credentials
            FirebaseOptions options;
            try {
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
            } catch (Exception e) {
                // Fallback to default credentials
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();
            }
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            firebaseAuth = FirebaseAuth.getInstance();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
    
    public void deleteUser(String firebaseUid) throws FirebaseAuthException {
        if (firebaseUid != null && !firebaseUid.isEmpty()) {
            firebaseAuth.deleteUser(firebaseUid);
        }
    }
} 