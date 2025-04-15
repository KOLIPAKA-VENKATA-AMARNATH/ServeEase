package com.easyserve.server.controller;

import com.easyserve.server.model.Admin;
import com.easyserve.server.model.Customer;
import com.easyserve.server.model.ServiceProvider;
import com.easyserve.server.model.ApprovalStatus;
import com.easyserve.server.model.Booking;  // Add this import
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

            if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Username and password are required");
            }

            if ("ServeEase".equals(username) && "AGSP@2025".equals(password)) {
                Admin admin = new Admin();
                admin.setAdmin_id("admin");
                admin.setUsername("ServeEase");
                admin.setEmail("admin@serveease.com");
                admin.setRole("ADMIN");
                return ResponseEntity.ok(admin);
            }

            return ResponseEntity.badRequest().body("Invalid credentials");
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error during login: " + e.getMessage());
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

    @GetMapping("/service-providers/pending")
    public ResponseEntity<?> getPendingServiceProviders() {
        try {
            if (!isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body("Authentication required");
            }

            List<ServiceProvider> pendingProviders = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("serviceProviders")
                .whereEqualTo("approvalStatus", "PENDING")
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
                provider.setApprovalStatus("PENDING");
                provider.setActive(false);
                
                pendingProviders.add(provider);
            }

            return ResponseEntity.ok(pendingProviders);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error fetching pending service providers: " + e.getMessage());
        }
    }

    // Helper method for authentication
    private boolean isAuthenticated() {
        // Implement your authentication logic here
        // This is just a placeholder
        return true;
    }

    @PutMapping("/service-providers/{providerId}/approval")
    public ResponseEntity<?> updateProviderApprovalStatus(
            @PathVariable String providerId,
            @RequestBody Map<String, String> approvalData) {
        try {
            // Validate providerId
            if (providerId == null || providerId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provider ID is required"));
            }

            String status = approvalData.get("status");
            if (status == null || (!status.equals(ApprovalStatus.APPROVED.name()) && 
                                 !status.equals(ApprovalStatus.REJECTED.name()))) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid approval status. Must be APPROVED or REJECTED"));
            }

            // First check if provider exists
            var providerDoc = firebaseService.getFirestore()
                .collection("serviceProviders")
                .document(providerId);

            var docSnapshot = providerDoc.get().get();
            if (!docSnapshot.exists()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "Service provider not found with ID: " + providerId));
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("approvalStatus", status);
            updates.put("active", status.equals(ApprovalStatus.APPROVED.name()));

            providerDoc.update(updates).get();

            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Service provider status updated successfully",
                    "providerId", providerId,
                    "status", status
                ));
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error updating provider status: " + e.getMessage()));
        }
    }
    
    // Add this method before the final closing brace
    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings() {
        try {
            if (!isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body("Authentication required");
            }

            List<Booking> bookings = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("bookings")
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                Booking booking = new Booking();
                
                booking.setBooking_id((String) data.get("booking_id"));
                booking.setCustomer_id((String) data.get("customer_id"));
                booking.setService_provider_id((String) data.get("service_provider_id"));
                booking.setService_type((String) data.get("service_type"));
                booking.setDescription((String) data.get("description"));
                booking.setStatus((String) data.get("status"));
                booking.setBookingDate((String) data.get("bookingDate"));
                booking.setScheduledDate((String) data.get("scheduledDate"));
                booking.setScheduledTime((String) data.get("scheduledTime"));
                
                bookings.add(booking);
            }

            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error fetching bookings: " + e.getMessage());
        }
    }
    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers() {
        try {
            if (!isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body("Authentication required");
            }

            List<Customer> customers = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("customers")
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                Customer customer = new Customer();
                
                customer.setCustomer_id((String) data.get("customer_id"));
                customer.setName((String) data.get("name"));
                customer.setEmail((String) data.get("email"));
                customer.setPhone((String) data.get("phone"));
                customer.setAddress((String) data.get("address"));
                customer.setVerified((Boolean) data.getOrDefault("isVerified", false));
                customer.setRegistrationDate((String) data.get("registrationDate"));
                customer.setBookings((List<String>) data.getOrDefault("bookings", new ArrayList<>()));
                
                customers.add(customer);
            }

            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error fetching customers: " + e.getMessage());
        }
    }

    @GetMapping("/service-providers/search")
    public ResponseEntity<?> searchServiceProvidersByName(@RequestParam String name) {
        try {
            List<ServiceProvider> providers = new ArrayList<>();
            String searchName = name.toLowerCase();
            
            var querySnapshot = firebaseService.getFirestore()
                .collection("serviceProviders")
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                String providerName = ((String) data.get("name")).toLowerCase();
                
                if (providerName.contains(searchName)) {
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
            }
            return ResponseEntity.ok(providers);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error searching providers: " + e.getMessage());
        }
    }

    @GetMapping("/service-providers/service")
    public ResponseEntity<?> searchServiceProvidersByService(@RequestParam String service) {
        try {
            List<ServiceProvider> providers = new ArrayList<>();
            String searchService = service.toLowerCase();
            
            var querySnapshot = firebaseService.getFirestore()
                .collection("serviceProviders")
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                List<String> services = ((List<String>) data.get("services"));
                
                boolean hasService = services.stream()
                    .anyMatch(s -> s.toLowerCase().contains(searchService));
                
                if (hasService) {
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
                    provider.setServices(services);
                    provider.setApprovalStatus((String) data.getOrDefault("approvalStatus", "PENDING"));
                    provider.setActive((Boolean) data.getOrDefault("active", false));
                    providers.add(provider);
                }
            }
            return ResponseEntity.ok(providers);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error searching by service: " + e.getMessage());
        }
    }

    @PutMapping("/announcements")
    public ResponseEntity<?> updateAnnouncement(@RequestBody Map<String, String> announcementData) {
        try {
            String message = announcementData.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Announcement message is required"));
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("message", message);
            updates.put("timestamp", java.time.Instant.now().toString());

            firebaseService.getFirestore()
                .collection("announcements")
                .document("current")
                .set(updates)
                .get();

            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Announcement updated successfully",
                    "announcement", updates
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error updating announcement: " + e.getMessage()));
        }
    }

    // Keep the GET method for retrieving announcements
    @GetMapping("/announcements")
    public ResponseEntity<?> getAnnouncements() {
        try {
            var docSnapshot = firebaseService.getFirestore()
                .collection("announcements")
                .document("current")
                .get()
                .get();

            if (!docSnapshot.exists()) {
                return ResponseEntity.ok(Map.of("message", "No announcements"));
            }

            return ResponseEntity.ok(docSnapshot.getData());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching announcements: " + e.getMessage());
        }
    }
}
