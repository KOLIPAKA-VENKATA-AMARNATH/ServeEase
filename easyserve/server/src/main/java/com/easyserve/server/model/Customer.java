package com.easyserve.server.model;

import java.util.List;

public class Customer {
    private String customer_id;
    private String name;
    private String email;
    private String password; // Add this field
    private String phone;
    private String address;
    private boolean isVerified;
    private String registrationDate;
    private List<String> bookings;
    
    // Default constructor
    public Customer() {
    }

    // All-args constructor (removed password)
    public Customer(String customer_id, String name, String email, String phone, 
            String address, boolean isVerified, String registrationDate, List<String> bookings) {
        this.customer_id = customer_id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.isVerified = isVerified;
        this.registrationDate = registrationDate;
        this.bookings = bookings;
    }

    // Getters and Setters
    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public List<String> getBookings() {
        return bookings;
    }

    public void setBookings(List<String> bookings) {
        this.bookings = bookings;
    }

    // Add password getter and setter
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Update toString to include password (masked)
    @Override
    public String toString() {
        return "Customer{" +
                "customer_id='" + customer_id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='****'" + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", isVerified=" + isVerified +
                ", registrationDate='" + registrationDate + '\'' +
                ", bookings=" + bookings +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return customer_id.equals(customer.customer_id);
    }

    @Override
    public int hashCode() {
        return customer_id.hashCode();
    }
}
