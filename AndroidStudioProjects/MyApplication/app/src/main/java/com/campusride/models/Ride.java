package com.campusride.models;

import java.util.HashMap;
import java.util.Map;

public class Ride {
    private String rideId;
    private String driverId;
    private String driverName;
    private String driverRegNo;
    private String source;
    private String destination;
    private String date;
    private String time;
    private String timestamp;
    private Map<String, Boolean> passengers; // Map of passenger IDs to request status
    
    // Required empty constructor for Firebase
    public Ride() {}
    
    public Ride(String rideId, String driverId, String driverName, String driverRegNo, 
                String source, String destination, String date, String time, String timestamp) {
        this.rideId = rideId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverRegNo = driverRegNo;
        this.source = source;
        this.destination = destination;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
        this.passengers = new HashMap<>();
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
    
    public String getDriverRegNo() {
        return driverRegNo;
    }
    
    public void setDriverRegNo(String driverRegNo) {
        this.driverRegNo = driverRegNo;
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
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Boolean> getPassengers() {
        return passengers;
    }
    
    public void setPassengers(Map<String, Boolean> passengers) {
        this.passengers = passengers;
    }
}