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
            // Validate required fields
            if (customer.getEmail() == null || customer.getEmail().trim().isEmpty() ||
                customer.getName() == null || customer.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Name and email are required fields");
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
            customerData.put("phone", customer.getPhone());
            customerData.put("address", customer.getAddress());
            customerData.put("isVerified", customer.isVerified());
            customerData.put("registrationDate", customer.getRegistrationDate());
            customerData.put("bookings", customer.getBookings());

            firebaseService.getFirestore()
                .collection("customers")
                .document(customerId)
                .set(customerData)
                .get();

            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating customer: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCustomers() {
        try {
            List<Customer> customers = new ArrayList<>();
            var querySnapshot = firebaseService.getFirestore()
                .collection("customers")
                .get()
                .get();

            for (var doc : querySnapshot.getDocuments()) {
                try {
                    Map<String, Object> data = doc.getData();
                    Customer customer = new Customer();
                    
                    // Debug log
                    System.out.println("Processing customer document: " + doc.getId());
                    System.out.println("Document data: " + data);
                    
                    // Safe type conversion with null checks
                    Object customerId = data.get("customer_id");
                    customer.setCustomer_id(customerId != null ? customerId.toString() : doc.getId());
                    
                    Object name = data.get("name");
                    customer.setName(name != null ? name.toString() : null);
                    
                    Object email = data.get("email");
                    customer.setEmail(email != null ? email.toString() : null);
                    
                    Object phone = data.get("phone");
                    customer.setPhone(phone != null ? phone.toString() : null);
                    
                    Object address = data.get("address");
                    customer.setAddress(address != null ? address.toString() : null);
                    
                    // Handle boolean
                    Object verified = data.get("isVerified");
                    customer.setVerified(verified != null ? Boolean.valueOf(verified.toString()) : false);
                    
                    // Handle date
                    Object regDate = data.get("registrationDate");
                    customer.setRegistrationDate(regDate != null ? regDate.toString() : null);
                    
                    // Handle bookings list
                    customer.setBookings(new ArrayList<>());
                    
                    customers.add(customer);
                } catch (Exception e) {
                    // Log individual document processing errors but continue with other documents
                    System.err.println("Error processing document: " + doc.getId());
                    e.printStackTrace();
                }
            }

            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            System.err.println("Critical error in getAllCustomers:");
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error fetching customers: " + e.getMessage());
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
}
