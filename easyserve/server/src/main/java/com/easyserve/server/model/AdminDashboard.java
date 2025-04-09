package com.easyserve.server.model;

public class AdminDashboard {
    private int totalProviders;
    private int pendingProviders;
    private int totalCustomers;
    private int activeBookings;

    // Getters and Setters
    public int getTotalProviders() {
        return totalProviders;
    }

    public void setTotalProviders(int totalProviders) {
        this.totalProviders = totalProviders;
    }

    public int getPendingProviders() {
        return pendingProviders;
    }

    public void setPendingProviders(int pendingProviders) {
        this.pendingProviders = pendingProviders;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public int getActiveBookings() {
        return activeBookings;
    }

    public void setActiveBookings(int activeBookings) {
        this.activeBookings = activeBookings;
    }
}