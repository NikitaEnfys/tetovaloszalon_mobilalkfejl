package com.example.inkturnejava;

import java.io.Serializable;

public class Appointment implements Serializable {
    private String salonId;
    private String userId;
    private String userEmail;
    private String timestamp;
    private String message;
    private String status; // pl. "pending", "accepted", "rejected"

    public Appointment() {
    }

    public Appointment(String salonId, String userId, String userEmail, String timestamp, String message, String status) {
        this.salonId = salonId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.timestamp = timestamp;
        this.message = message;
        this.status = status;
    }


    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
