package com.easyserve.server.controller;

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
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            // Validate required fields
            if (booking.getCustomer_id() == null || booking.getService_provider_id() == null ||
                booking.getService_type() == null || booking.getScheduledDate() == null ||
                booking.getScheduledTime() == null) {
                return ResponseEntity.badRequest()
                    .body("All required fields must be provided");
            }

            // Check if service provider is approved
            var providerDoc = firebaseService.getFirestore()
                .collection("serviceProviders")
                .document(booking.getService_provider_id())
                .get()
                .get();

            if (!providerDoc.exists()) {
                return ResponseEntity.badRequest()
                    .body("Service provider not found");
            }

            String approvalStatus = (String) providerDoc.getData().get("approvalStatus");
            if (!"APPROVED".equals(approvalStatus)) {
                return ResponseEntity.badRequest()
                    .body("Cannot book with unapproved service provider");
            }

            String bookingId = UUID.randomUUID().toString();
            booking.setBooking_id(bookingId);
            booking.setBookingDate(LocalDateTime.now().toString());
            booking.setStatus("PENDING");

            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("booking_id", booking.getBooking_id());
            bookingData.put("customer_id", booking.getCustomer_id());
            bookingData.put("service_provider_id", booking.getService_provider_id());
            bookingData.put("service_type", booking.getService_type());
            bookingData.put("description", booking.getDescription());
            bookingData.put("status", booking.getStatus());
            bookingData.put("bookingDate", booking.getBookingDate());
            bookingData.put("scheduledDate", booking.getScheduledDate());
            bookingData.put("scheduledTime", booking.getScheduledTime());

            firebaseService.getFirestore()
                .collection("bookings")
                .document(bookingId)
                .set(bookingData)
                .get();

            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error creating booking: " + e.getMessage());
        }
    }

    @PutMapping("/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable String bookingId,
            @RequestParam String status) {
        try {
            firebaseService.getFirestore()
                .collection("bookings")
                .document(bookingId)
                .update("status", status)
                .get();

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error updating booking status: " + e.getMessage());
        }
    }

    @PutMapping("/{bookingId}/provider-approval")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> updateProviderBookingApproval(
            @PathVariable String bookingId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null || !status.matches("ACCEPTED|REJECTED")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Status must be either ACCEPTED or REJECTED"));
            }

            var bookingDoc = firebaseService.getFirestore()
                .collection("bookings")
                .document(bookingId)
                .get()
                .get();

            if (!bookingDoc.exists()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            updates.put("updatedAt", LocalDateTime.now().toString());

            firebaseService.getFirestore()
                .collection("bookings")
                .document(bookingId)
                .update(updates)
                .get();

            return ResponseEntity.ok()
                .body(Map.of("message", "Booking " + status.toLowerCase() + " successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error updating booking status: " + e.getMessage());
        }
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getProviderBookings(@PathVariable String providerId) {
        try {
            System.out.println("Fetching bookings for provider: " + providerId); // Debug log
            
            List<Booking> bookings = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("bookings")
                .whereEqualTo("service_provider_id", providerId)
                .get()
                .get();

            System.out.println("Found " + querySnapshot.size() + " bookings"); // Debug log

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                System.out.println("Booking data: " + data); // Debug log
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
                .body("Error fetching provider bookings: " + e.getMessage());
        }
    }

    // Add this new endpoint for customer bookings
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerBookings(@PathVariable String customerId) {
        try {
            System.out.println("Fetching bookings for customer: " + customerId);
            
            List<Booking> bookings = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("bookings")
                .whereEqualTo("customer_id", customerId)
                .get()
                .get();

            System.out.println("Found " + querySnapshot.size() + " bookings");

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
                .body("Error fetching customer bookings: " + e.getMessage());
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllBookingsForAdmin() {
        try {
            System.out.println("Fetching all bookings for admin");
            
            List<Map<String, Object>> bookingsWithDetails = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("bookings")
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                
                // Get customer details
                var customerDoc = firebaseService.getFirestore()
                    .collection("customers")
                    .document((String) data.get("customer_id"))
                    .get()
                    .get();

                // Get service provider details
                var providerDoc = firebaseService.getFirestore()
                    .collection("serviceProviders")
                    .document((String) data.get("service_provider_id"))
                    .get()
                    .get();

                Map<String, Object> bookingWithDetails = new HashMap<>(data);
                
                // Add customer details
                if (customerDoc.exists()) {
                    bookingWithDetails.put("customerName", customerDoc.getString("name"));
                    bookingWithDetails.put("customerEmail", customerDoc.getString("email"));
                    bookingWithDetails.put("customerPhone", customerDoc.getString("phone"));
                }

                // Add service provider details
                if (providerDoc.exists()) {
                    bookingWithDetails.put("providerName", providerDoc.getString("name"));
                    bookingWithDetails.put("providerService", providerDoc.getString("service_type"));
                    bookingWithDetails.put("providerPhone", providerDoc.getString("phone"));
                }

                bookingsWithDetails.add(bookingWithDetails);
            }

            return ResponseEntity.ok(bookingsWithDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error fetching bookings for admin: " + e.getMessage());
        }
    }

    @PostMapping("/{bookingId}/review")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> addBookingReview(
            @PathVariable String bookingId,
            @RequestBody Map<String, Object> review) {
        try {
            // Enhanced validation
            if (!review.containsKey("rating") || !review.containsKey("comment")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Rating and comment are required"));
            }

            // Validate rating is between 1 and 5
            int rating = Integer.parseInt(review.get("rating").toString());
            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Rating must be between 1 and 5"));
            }

            // Validate comment is not empty
            String comment = (String) review.get("comment");
            if (comment == null || comment.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Comment cannot be empty"));
            }

            var bookingDoc = firebaseService.getFirestore()
                .collection("bookings")
                .document(bookingId)
                .get()
                .get();

            if (!bookingDoc.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Check if review already exists
            Map<String, Object> bookingData = bookingDoc.getData();
            if (bookingData.containsKey("review")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Review already exists for this booking"));
            }

            // Create review data with customer ID
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("rating", rating);
            reviewData.put("comment", comment);
            reviewData.put("reviewDate", LocalDateTime.now().toString());
            reviewData.put("customer_id", bookingData.get("customer_id"));

            // Update booking with review
            firebaseService.getFirestore()
                .collection("bookings")
                .document(bookingId)
                .update("review", reviewData)
                .get();

            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Review added successfully",
                    "review", reviewData
                ));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Rating must be a number between 1 and 5"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error adding review: " + e.getMessage());
        }
    }

    // Endpoint for customers to view their own reviews
    @GetMapping("/customer/{customerId}/reviews")
    public ResponseEntity<?> getCustomerReviews(@PathVariable String customerId) {
        try {
            List<Map<String, Object>> reviews = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("bookings")
                .whereEqualTo("customer_id", customerId)
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data.containsKey("review")) {
                    Map<String, Object> reviewWithDetails = new HashMap<>(data);
                    var providerDoc = firebaseService.getFirestore()
                        .collection("serviceProviders")
                        .document((String) data.get("service_provider_id"))
                        .get()
                        .get();
                    
                    if (providerDoc.exists()) {
                        reviewWithDetails.put("providerName", providerDoc.getString("name"));
                        reviewWithDetails.put("providerService", providerDoc.getString("service_type"));
                    }
                    reviews.add(reviewWithDetails);
                }
            }
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error fetching customer reviews: " + e.getMessage());
        }
    }

    // Endpoint for service providers to view reviews they received
    @GetMapping("/provider/{providerId}/reviews")
    public ResponseEntity<?> getProviderReviews(@PathVariable String providerId) {
        try {
            List<Map<String, Object>> reviews = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("bookings")
                .whereEqualTo("service_provider_id", providerId)
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data.containsKey("review")) {
                    Map<String, Object> reviewWithDetails = new HashMap<>(data);
                    var customerDoc = firebaseService.getFirestore()
                        .collection("customers")
                        .document((String) data.get("customer_id"))
                        .get()
                        .get();
                    
                    if (customerDoc.exists()) {
                        reviewWithDetails.put("customerName", customerDoc.getString("name"));
                    }
                    reviews.add(reviewWithDetails);
                }
            }
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error fetching provider reviews: " + e.getMessage());
        }
    }

    // Endpoint for admin to view all reviews
    @GetMapping("/admin/reviews")
    public ResponseEntity<?> getAllReviews() {
        try {
            List<Map<String, Object>> allReviews = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("bookings")
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data.containsKey("review")) {
                    Map<String, Object> reviewWithDetails = new HashMap<>(data);
                    
                    // Get customer details
                    var customerDoc = firebaseService.getFirestore()
                        .collection("customers")
                        .document((String) data.get("customer_id"))
                        .get()
                        .get();

                    // Get provider details
                    var providerDoc = firebaseService.getFirestore()
                        .collection("serviceProviders")
                        .document((String) data.get("service_provider_id"))
                        .get()
                        .get();

                    if (customerDoc.exists()) {
                        reviewWithDetails.put("customerName", customerDoc.getString("name"));
                        reviewWithDetails.put("customerEmail", customerDoc.getString("email"));
                    }

                    if (providerDoc.exists()) {
                        reviewWithDetails.put("providerName", providerDoc.getString("name"));
                        reviewWithDetails.put("providerService", providerDoc.getString("service_type"));
                    }

                    allReviews.add(reviewWithDetails);
                }
            }
            return ResponseEntity.ok(allReviews);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error fetching all reviews: " + e.getMessage());
        }
    }
    
}
