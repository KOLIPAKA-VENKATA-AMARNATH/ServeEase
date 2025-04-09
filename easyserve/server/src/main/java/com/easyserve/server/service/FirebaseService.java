package com.easyserve.server.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {
    
    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
}