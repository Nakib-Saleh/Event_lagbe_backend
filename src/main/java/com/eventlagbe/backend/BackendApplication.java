package com.eventlagbe.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		// Log environment variables for debugging
		System.out.println("=== Railway Deployment Debug Info ===");
		System.out.println("SPRING_PROFILES_ACTIVE: " + System.getenv("SPRING_PROFILES_ACTIVE"));
		System.out.println("MONGODB_URI: " + (System.getenv("MONGODB_URI") != null ? "SET" : "NOT SET"));
		System.out.println("MAIL_HOST: " + (System.getenv("MAIL_HOST") != null ? "SET" : "NOT SET"));
		System.out.println("MAIL_USERNAME: " + (System.getenv("MAIL_USERNAME") != null ? "SET" : "NOT SET"));
		System.out.println("FIREBASE_SERVICE_ACCOUNT_JSON: " + (System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON") != null ? "SET" : "NOT SET"));
		System.out.println("PORT: " + System.getenv("PORT"));
		System.out.println("=====================================");
		
		SpringApplication.run(BackendApplication.class, args);
	}

}
