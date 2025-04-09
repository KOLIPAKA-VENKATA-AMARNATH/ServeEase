package com.easyserve.server.controller;

import com.easyserve.server.model.Review;
import com.easyserve.server.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestBody Review review) {
        try {
            String reviewId = UUID.randomUUID().toString();
            review.setReviewId(reviewId);  // Updated method name
            review.setDate(LocalDateTime.now().toString());

            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("review_id", review.getReviewId());     // Updated method name
            reviewData.put("booking_id", review.getBookingId());   // Updated method name
            reviewData.put("customer_id", review.getCustomerId()); // Updated method name
            reviewData.put("provider_id", review.getProviderId()); // Updated method name
            reviewData.put("rating", review.getRating());
            reviewData.put("comment", review.getComment());
            reviewData.put("date", review.getDate());

            firebaseService.getFirestore()
                .collection("reviews")
                .document(reviewId)
                .set(reviewData)
                .get();

            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating review: " + e.getMessage());
        }
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getProviderReviews(@PathVariable String providerId) {
        try {
            List<Map<String, Object>> reviews = new ArrayList<>();
            double totalRating = 0;
            
            var querySnapshot = firebaseService.getFirestore()
                .collection("reviews")
                .whereEqualTo("provider_id", providerId)
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                try {
                    Map<String, Object> reviewData = new HashMap<>();
                    reviewData.put("review_id", data.get("review_id"));
                    reviewData.put("booking_id", data.get("booking_id"));
                    reviewData.put("customer_id", data.get("customer_id"));
                    reviewData.put("provider_id", data.get("provider_id"));
                    reviewData.put("rating", data.get("rating") != null ? 
                        ((Long) data.get("rating")).intValue() : 0);
                    reviewData.put("comment", data.get("comment"));
                    reviewData.put("date", data.get("date"));
                    
                    reviews.add(reviewData);
                    totalRating += ((Long) data.get("rating")).intValue();
                } catch (Exception e) {
                    continue; // Skip malformed reviews
                }
            }

            double averageRating = reviews.isEmpty() ? 0 : totalRating / reviews.size();

            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviews);
            response.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
            response.put("totalReviews", reviews.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error fetching reviews: " + e.getMessage()));
        }
    }

    private Review mapToReview(Map<String, Object> data) {
        Review review = new Review();
        try {
            review.setReviewId((String) data.get("review_id"));
            review.setBookingId((String) data.get("booking_id"));
            review.setCustomerId((String) data.get("customer_id"));
            review.setProviderId((String) data.get("provider_id"));
            
            // Handle rating conversion safely
            Object ratingObj = data.get("rating");
            if (ratingObj != null) {
                if (ratingObj instanceof Long) {
                    review.setRating(((Long) ratingObj).intValue());
                } else if (ratingObj instanceof Integer) {
                    review.setRating((Integer) ratingObj);
                }
            }
            
            review.setComment((String) data.get("comment"));
            review.setDate((String) data.get("date"));
        } catch (Exception e) {
            System.err.println("Error mapping review data: " + e.getMessage());
        }
        return review;
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getBookingReview(@PathVariable String bookingId) {
        try {
            var querySnapshot = firebaseService.getFirestore()
                .collection("reviews")
                .whereEqualTo("booking_id", bookingId)
                .get()
                .get();

            if (querySnapshot.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var doc = querySnapshot.getDocuments().get(0);
            return ResponseEntity.ok(mapToReview(doc.getData()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error fetching review: " + e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerReviews(@PathVariable String customerId) {
        try {
            List<Review> reviews = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("reviews")
                .whereEqualTo("customer_id", customerId)
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                reviews.add(mapToReview(doc.getData()));
            }

            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error fetching reviews: " + e.getMessage()));
        }
    }
}
