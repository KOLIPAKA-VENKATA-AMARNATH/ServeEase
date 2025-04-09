package com.easyserve.server.model;

public class Booking {
    private String booking_id;
    private String customer_id;
    private String service_provider_id;
    private String service_type;
    private String description;
    private String status;
    private String bookingDate;
    private String scheduledDate;
    private String scheduledTime;

    // Default constructor
    public Booking() {
    }

    // All-args constructor
    public Booking(String booking_id, String customer_id, String service_provider_id, 
            String service_type, String description, String status, String bookingDate, 
            String scheduledDate, String scheduledTime) {
        this.booking_id = booking_id;
        this.customer_id = customer_id;
        this.service_provider_id = service_provider_id;
        this.service_type = service_type;
        this.description = description;
        this.status = status;
        this.bookingDate = bookingDate;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
    }

    // Getters and Setters
    public String getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(String booking_id) {
        this.booking_id = booking_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getService_provider_id() {
        return service_provider_id;
    }

    public void setService_provider_id(String service_provider_id) {
        this.service_provider_id = service_provider_id;
    }

    public String getService_type() {
        return service_type;
    }

    public void setService_type(String service_type) {
        this.service_type = service_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "booking_id='" + booking_id + '\'' +
                ", customer_id='" + customer_id + '\'' +
                ", service_provider_id='" + service_provider_id + '\'' +
                ", service_type='" + service_type + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", bookingDate='" + bookingDate + '\'' +
                ", scheduledDate='" + scheduledDate + '\'' +
                ", scheduledTime='" + scheduledTime + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return booking_id.equals(booking.booking_id);
    }

    @Override
    public int hashCode() {
        return booking_id.hashCode();
    }
}