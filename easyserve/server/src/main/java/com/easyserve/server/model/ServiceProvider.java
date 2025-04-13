package com.easyserve.server.model;

import java.util.List;

public class ServiceProvider {
    private String provider_id;
    private String name;
    private String phone;
    private String email;
    private String adhar;
    private String address;
    private String gender;
    private int age;
    private String about;
    private List<String> services;
    private String approvalStatus = "PENDING";
    private boolean active = false;

    // Default constructor
    public ServiceProvider() {
    }

    // Constructor with main fields
    public ServiceProvider(String provider_id, String name, String phone, String email, 
            String adhar, String address, String gender, int age, String about) {
        this.provider_id = provider_id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.adhar = adhar;
        this.address = address;
        this.gender = gender;
        this.age = age;
        this.about = about;
    }

    // Getters and Setters
    public String getProvider_id() {
        return provider_id;
    }

    public void setProvider_id(String provider_id) {
        this.provider_id = provider_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdhar() {
        return adhar;
    }

    public void setAdhar(String adhar) {
        this.adhar = adhar;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Add these fields after existing fields
    private double averageRating = 0.0;
    private int totalReviews = 0;
    private List<Review> reviews;

    // Add these getters and setters after existing ones
    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    // Add this field with other private fields at the top
    private String password;

    // Add this getter and setter with other getters/setters
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
