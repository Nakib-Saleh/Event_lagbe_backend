package com.eventlagbe.backend.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import jakarta.annotation.PostConstruct;

@Configuration
@PropertySource("classpath:application-railway.properties")
public class EnvironmentConfig {

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${firebase.service-account.path:}")
    private String firebasePath;

    @PostConstruct
    public void logEnvironmentVariables() {
        System.out.println("=== Environment Configuration ===");
        System.out.println("MongoDB URI: " + (mongoUri != null && !mongoUri.isEmpty() ? "SET" : "NOT SET"));
        System.out.println("Mail Host: " + (mailHost != null && !mailHost.isEmpty() ? "SET" : "NOT SET"));
        System.out.println("Mail Username: " + (mailUsername != null && !mailUsername.isEmpty() ? "SET" : "NOT SET"));
        System.out.println("Firebase Path: " + firebasePath);
        System.out.println("FIREBASE_SERVICE_ACCOUNT_JSON: " + (System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON") != null ? "SET" : "NOT SET"));
        System.out.println("MONGODB_URI: " + (System.getenv("MONGODB_URI") != null ? "SET" : "NOT SET"));
        System.out.println("MAIL_HOST: " + (System.getenv("MAIL_HOST") != null ? "SET" : "NOT SET"));
        System.out.println("MAIL_USERNAME: " + (System.getenv("MAIL_USERNAME") != null ? "SET" : "NOT SET"));
        System.out.println("SPRING_PROFILES_ACTIVE: " + System.getenv("SPRING_PROFILES_ACTIVE"));
        System.out.println("==================================");
    }
}
