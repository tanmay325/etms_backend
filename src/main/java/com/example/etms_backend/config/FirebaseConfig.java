package com.example.etms_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws Exception {

        String projectId = System.getenv("FIREBASE_PROJECT_ID");
        String clientEmail = System.getenv("FIREBASE_CLIENT_EMAIL");
        String privateKey = System.getenv("FIREBASE_PRIVATE_KEY");

        // Important: fix line breaks
        privateKey = privateKey.replace("\\n", "\n");

        String firebaseConfig = "{"
                + "\"type\": \"service_account\","
                + "\"project_id\": \"" + projectId + "\","
                + "\"private_key\": \"" + privateKey + "\","
                + "\"client_email\": \"" + clientEmail + "\""
                + "}";

        ByteArrayInputStream serviceAccount =
                new ByteArrayInputStream(firebaseConfig.getBytes(StandardCharsets.UTF_8));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}
