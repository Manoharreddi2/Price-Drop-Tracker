package com.watchdog.pricedrop.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount;

            // Check for FIREBASE_CONFIG env var first (for Render/cloud deployment)
            String firebaseConfigJson = System.getenv("FIREBASE_CONFIG");

            if (firebaseConfigJson != null && !firebaseConfigJson.isEmpty()) {
                logger.info("Using FIREBASE_CONFIG environment variable");
                serviceAccount = new ByteArrayInputStream(
                        firebaseConfigJson.getBytes(StandardCharsets.UTF_8)
                );
            } else {
                // Fall back to file (for local development)
                logger.info("Using local serviceAccountKey.json file");
                serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream(firebaseConfigPath);

                if (serviceAccount == null) {
                    throw new IOException("Firebase config not found. Set FIREBASE_CONFIG env var or provide " + firebaseConfigPath);
                }
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}
