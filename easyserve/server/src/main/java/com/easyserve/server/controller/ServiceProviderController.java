package com.easyserve.server.controller;

import com.easyserve.server.model.ServiceProvider;
import com.easyserve.server.model.Booking;
import com.easyserve.server.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/service-providers")
@CrossOrigin(origins = "*")
public class ServiceProviderController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody ServiceProvider provider) {
        try {
            String providerId = UUID.randomUUID().toString();
            
            ServiceProvider newProvider = new ServiceProvider(
                providerId,
                provider.getName(),
                provider.getPhone(),
                provider.getEmail(),
                provider.getAdhar(),
                provider.getAddress(),
                provider.getGender(),
                provider.getAge(),
                provider.getAbout()
            );
            
            newProvider.setServices(provider.getServices());
            
            Map<String, Object> providerData = new HashMap<>();
            providerData.put("provider_id", newProvider.getProvider_id());
            providerData.put("name", newProvider.getName());
            providerData.put("email", newProvider.getEmail());
            providerData.put("phone", newProvider.getPhone());
            providerData.put("adhar", newProvider.getAdhar());
            providerData.put("address", newProvider.getAddress());
            providerData.put("gender", newProvider.getGender());
            providerData.put("age", newProvider.getAge());
            providerData.put("about", newProvider.getAbout());
            providerData.put("services", newProvider.getServices());
            providerData.put("approvalStatus", newProvider.getApprovalStatus());
            providerData.put("active", newProvider.isActive());
            providerData.put("password", provider.getPassword());

            firebaseService.getFirestore()
                .collection("serviceProviders")
                .document(providerId)
                .set(providerData)
                .get();

            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Registration successful. Please wait for admin approval.",
                    "provider", newProvider
                ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error during registration: " + e.getMessage());
        }
    }

    @GetMapping("/bookings/{providerId}")
    public ResponseEntity<?> getProviderBookings(@PathVariable String providerId) {
        try {
            var providerDoc = firebaseService.getFirestore()
                .collection("serviceProviders")
                .document(providerId)
                .get()
                .get();

            if (!providerDoc.exists()) {
                return ResponseEntity.notFound().build();
            }

            String approvalStatus = providerDoc.getString("approvalStatus");
            if (!"APPROVED".equals(approvalStatus)) {
                return ResponseEntity.status(403)
                    .body("Access denied. Please wait for admin approval.");
            }

            List<Booking> bookings = new ArrayList<>();
            var bookingDocs = firebaseService.getFirestore()
                .collection("bookings")
                .whereEqualTo("service_provider_id", providerId)
                .get()
                .get();

            for (var doc : bookingDocs.getDocuments()) {
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
            return ResponseEntity.internalServerError().body("Error fetching bookings: " + e.getMessage());
        }
    }

    @PutMapping("/{providerId}/approval")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> updateProviderApproval(
            @PathVariable("providerId") String providerId,
            @RequestBody Map<String, String> request) {
        try {
            String approvalStatus = request.get("approvalStatus");
            System.out.println("Received request - Provider ID: " + providerId + ", Status: " + approvalStatus);

            if (approvalStatus == null || !approvalStatus.matches("APPROVED|REJECTED|PENDING")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid approval status. Must be APPROVED, REJECTED, or PENDING"));
            }

            var providerDoc = firebaseService.getFirestore()
                .collection("serviceProviders")
                .document(providerId)
                .get()
                .get();

            if (!providerDoc.exists()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("approvalStatus", approvalStatus);
            updates.put("active", approvalStatus.equals("APPROVED"));

            firebaseService.getFirestore()
                .collection("serviceProviders")
                .document(providerId)
                .update(updates)
                .get();

            return ResponseEntity.ok()
                .body(Map.of("message", "Provider approval status updated successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error updating provider approval status: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> loginProvider(@RequestBody Map<String, String> loginRequest) {
        try {
            String name = loginRequest.get("name");
            String password = loginRequest.get("password");

            if (name == null || name.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Name and password are required");
            }

            var querySnapshot = firebaseService.getFirestore()
                .collection("serviceProviders")
                .whereEqualTo("name", name)
                .get()
                .get();

            if (querySnapshot.isEmpty()) {
                return ResponseEntity.status(401).body("Invalid credentials");
            }

            var providerDoc = querySnapshot.getDocuments().get(0);
            Map<String, Object> data = providerDoc.getData();

            String storedPassword = (String) data.get("password");
            if (storedPassword == null || !password.equals(storedPassword)) {
                return ResponseEntity.status(401).body("Invalid credentials");
            }

            ServiceProvider provider = new ServiceProvider(
                (String) data.get("provider_id"),
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
            provider.setApprovalStatus((String) data.get("approvalStatus"));
            provider.setActive((Boolean) data.getOrDefault("active", false));

            return ResponseEntity.ok(provider);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error during login: " + e.getMessage());
        }
    }
}
