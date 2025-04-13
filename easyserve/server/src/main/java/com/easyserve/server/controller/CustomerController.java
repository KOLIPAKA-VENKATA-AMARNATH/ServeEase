package com.easyserve.server.controller;

import com.easyserve.server.model.Customer;
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
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/signup")
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        try {
            // Validate required fields including password
            if (customer.getEmail() == null || customer.getEmail().trim().isEmpty() ||
                customer.getName() == null || customer.getName().trim().isEmpty() ||
                customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Name, email and password are required fields");
            }

            // Check if email already exists
            var existingCustomer = firebaseService.getFirestore()
                .collection("customers")
                .whereEqualTo("email", customer.getEmail())
                .get()
                .get();

            if (!existingCustomer.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Email already registered");
            }

            String customerId = UUID.randomUUID().toString();
            customer.setCustomer_id(customerId);
            customer.setRegistrationDate(LocalDateTime.now().toString());
            customer.setVerified(false);
            customer.setBookings(new ArrayList<>());
            
            Map<String, Object> customerData = new HashMap<>();
            customerData.put("customer_id", customer.getCustomer_id());
            customerData.put("name", customer.getName());
            customerData.put("email", customer.getEmail());
            customerData.put("password", customer.getPassword());
            customerData.put("phone", customer.getPhone());
            customerData.put("address", customer.getAddress());
            customerData.put("isVerified", customer.isVerified());
            customerData.put("registrationDate", customer.getRegistrationDate());
            customerData.put("bookings", customer.getBookings());

            // Print debug info
            System.out.println("Storing customer with name: " + customer.getName());
            System.out.println("Password being stored: " + customer.getPassword());

            // Store in Firebase
            firebaseService.getFirestore()
                .collection("customers")
                .document(customer.getCustomer_id())
                .set(customerData)
                .get();

            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating customer: " + e.getMessage());
        }
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<?> getCustomerById(@PathVariable String customerId) {
        try {
            var docSnapshot = firebaseService.getFirestore()
                .collection("customers")
                .document(customerId)
                .get()
                .get();

            if (!docSnapshot.exists()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> data = docSnapshot.getData();
            Customer customer = new Customer();
            customer.setCustomer_id(docSnapshot.getId());
            customer.setName((String) data.get("name"));
            customer.setEmail((String) data.get("email"));
            customer.setPhone((String) data.get("phone"));
            customer.setAddress((String) data.get("address"));
            customer.setVerified((Boolean) data.getOrDefault("isVerified", false));
            customer.setRegistrationDate((String) data.get("registrationDate"));
            customer.setBookings(new ArrayList<>());

            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error fetching customer: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> loginCustomer(@RequestBody Map<String, String> loginRequest) {
        try {
            String name = loginRequest.get("name");
            String password = loginRequest.get("password");

            // Add debug logging
            System.out.println("Login attempt - Name: " + name);

            var querySnapshot = firebaseService.getFirestore()
                .collection("customers")
                .whereEqualTo("name", name)
                .get()
                .get();

            if (querySnapshot.isEmpty()) {
                System.out.println("No customer found with name: " + name);
                return ResponseEntity.status(401).body("Invalid credentials");
            }

            var customerDoc = querySnapshot.getDocuments().get(0);
            Map<String, Object> data = customerDoc.getData();

            // Debug print
            System.out.println("Found customer data: " + data.toString());

            String storedPassword = (String) data.get("password");
            if (storedPassword == null || !password.equals(storedPassword)) {
                System.out.println("Password mismatch");
                return ResponseEntity.status(401).body("Invalid credentials");
            }

            // If we get here, credentials are valid
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }
    
    // Add this new endpoint after the existing methods
    @GetMapping("/service-providers")
    public ResponseEntity<?> getApprovedServiceProviders() {
        try {
            List<Map<String, Object>> providers = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("serviceProviders")
                .whereEqualTo("approvalStatus", "APPROVED")  // Changed from "ACCEPTED"
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                Map<String, Object> providerInfo = new HashMap<>();
                
                // Include only necessary public information
                providerInfo.put("provider_id", doc.getId());
                providerInfo.put("name", data.get("name"));
                providerInfo.put("services", data.get("services"));  // Changed from service_type
                providerInfo.put("experience", data.get("experience"));
                providerInfo.put("location", data.get("location"));
                providerInfo.put("phone", data.get("phone"));
                providerInfo.put("about", data.get("about"));
                
                providers.add(providerInfo);
            }

            return ResponseEntity.ok(providers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error fetching service providers: " + e.getMessage());
        }
    }
}

