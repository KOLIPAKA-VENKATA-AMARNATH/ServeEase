package com.easyserve.server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {
    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                System.out.println("Initializing Firebase...");
                ClassPathResource serviceAccount = new ClassPathResource("serviceAccountKey.json");
                
                if (!serviceAccount.exists()) {
                    throw new IOException("serviceAccountKey.json not found in resources");
                }
                
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount.getInputStream())
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");
                
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId("serveease-4cb36")
                    .build();
                    
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully!");
                
                // Verify credentials
                credentials.refresh();
                System.out.println("Credentials verified successfully!");
            }
        } catch (IOException e) {
            System.err.println("Firebase initialization error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error initializing Firebase: " + e.getMessage(), e);
        }
    }
}