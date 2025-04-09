package com.easyserve.server.controller;

import com.easyserve.server.model.Admin;
import com.easyserve.server.model.Customer;
import com.easyserve.server.model.ServiceProvider;
import com.easyserve.server.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            // Validate input
            if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Username and password are required");
            }

            // Check for default credentials
            if ("ServeEase".equals(username) && "AGSP@2025".equals(password)) {
                Admin admin = new Admin();
                admin.setAdmin_id("default-admin");
                admin.setUsername("ServeEase");
                admin.setEmail("admin@serveease.com");
                admin.setRole("ADMIN");
                return ResponseEntity.ok(admin);
            }

            return ResponseEntity.badRequest().body("Invalid credentials");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error during login: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createAdmin(@RequestBody Admin admin) {
        try {
            // Validate required fields
            if (admin.getEmail() == null || admin.getEmail().trim().isEmpty() ||
                admin.getPassword() == null || admin.getPassword().trim().isEmpty() ||
                admin.getUsername() == null || admin.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Email, password, and username are required fields");
            }

            // Check if email already exists
            var existingAdmin = firebaseService.getFirestore()
                .collection("admins")
                .whereEqualTo("email", admin.getEmail())
                .get()
                .get();

            if (!existingAdmin.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Email already registered");
            }

            String adminId = UUID.randomUUID().toString();
            admin.setAdmin_id(adminId);
            
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("admin_id", adminId);
            adminData.put("username", admin.getUsername());
            adminData.put("email", admin.getEmail());
            adminData.put("password", admin.getPassword());  // In production, should hash password
            adminData.put("role", admin.getRole());

            firebaseService.getFirestore()
                .collection("admins")
                .document(adminId)
                .set(adminData)
                .get();

            return ResponseEntity.ok(admin);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating admin: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllAdmins() {
        try {
            List<Admin> admins = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("admins")
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Admin admin = new Admin();
                admin.setAdmin_id(doc.getId());
                admin.setUsername((String) doc.get("username"));
                admin.setEmail((String) doc.get("email"));
                admin.setRole((String) doc.get("role"));
                admins.add(admin);
            }

            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error fetching admins: " + e.getMessage());
        }
    }

    @GetMapping("/{adminId}")
    public ResponseEntity<?> getAdminById(@PathVariable String adminId) {
        try {
            // Validate adminId
            if (adminId == null || adminId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Admin ID is required");
            }

            var docSnapshot = firebaseService.getFirestore()
                .collection("admins")
                .document(adminId)
                .get()
                .get();

            if (!docSnapshot.exists()) {
                return ResponseEntity.notFound().build();
            }

            Admin admin = new Admin();
            admin.setAdmin_id(docSnapshot.getId());
            admin.setUsername((String) docSnapshot.get("username"));
            admin.setEmail((String) docSnapshot.get("email"));
            admin.setRole((String) docSnapshot.get("role"));

            return ResponseEntity.ok(admin);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error fetching admin: " + e.getMessage());
        }
    }

    @GetMapping("/service-providers")
    public ResponseEntity<?> getAllServiceProviders() {
        try {
            if (!isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body("Authentication required");
            }

            List<ServiceProvider> providers = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("serviceProviders")  // Updated collection name
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                ServiceProvider provider = new ServiceProvider(
                    doc.getId(),
                    (String) data.get("name"),
                    (String) data.get("phone"),
                    (String) data.get("email"),
                    (String) data.get("adhar"),
                    (String) data.get("address"),
                    (String) data.get("gender"),
                    ((Long) data.get("age")).intValue(),
                    (String) data.get("about")
                );
                
                provider.setServices((List<String>) data.get("services"));
                provider.setApprovalStatus((String) data.getOrDefault("approvalStatus", "PENDING"));
                provider.setActive((Boolean) data.getOrDefault("active", false));
                
                providers.add(provider);
            }

            return ResponseEntity.ok(providers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error fetching service providers: " + e.getMessage());
        }
    }

    // Helper method for authentication
    private boolean isAuthenticated() {
        // Implement your authentication logic here
        // This is just a placeholder
        return true;
    }
}
