package com.campusride.models;

public class Ride {
    private String rideId;
    private String driverId;
    private String driverName;
    private String source;
    private String destination;
    private String date;
    private String time;
    private double sourceLat;
    private double sourceLng;
    private double destinationLat;
    private double destinationLng;
    private String status; // pending, active, completed, cancelled

    public Ride() {
        // Default constructor required for Firebase
    }

    public Ride(String rideId, String driverId, String driverName, String source, String destination,
                String date, String time, double sourceLat, double sourceLng,
                double destinationLat, double destinationLng) {
        this.rideId = rideId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.source = source;
        this.destination = destination;
        this.date = date;
        this.time = time;
        this.sourceLat = sourceLat;
        this.sourceLng = sourceLng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
        this.status = "pending";
    }

    // Getters and setters
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

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getSourceLat() {
        return sourceLat;
    }

    public void setSourceLat(double sourceLat) {
        this.sourceLat = sourceLat;
    }

    public double getSourceLng() {
        return sourceLng;
    }

    public void setSourceLng(double sourceLng) {
        this.sourceLng = sourceLng;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(double destinationLng) {
        this.destinationLng = destinationLng;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}