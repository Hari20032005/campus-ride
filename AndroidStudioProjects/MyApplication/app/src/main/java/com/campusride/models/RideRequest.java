package com.campusride.models;

public class RideRequest {
    private String requestId;
    private String rideId;
    private String driverId;  // Added to link request to specific driver
    private String passengerId;
    private String passengerName;
    private String status; // pending, accepted, rejected
    private double pickupLat;
    private double pickupLng;
    private String pickupLocation;

    public RideRequest() {
        // Default constructor required for Firebase
    }

    public RideRequest(String requestId, String rideId, String driverId, String passengerId, String passengerName,
                       double pickupLat, double pickupLng, String pickupLocation) {
        this.requestId = requestId;
        this.rideId = rideId;
        this.driverId = driverId;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.pickupLocation = pickupLocation;
        this.status = "pending";
    }

    // Getters and setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
}