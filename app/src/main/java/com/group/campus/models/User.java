package com.group.campus.models;

public class User {
    private String userId;
    private String name;
    private String email;
    private String role; // "student" or "staff"
    private String department;
    private String fcmToken;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String userId, String name, String email, String role, String department) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.department = department;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    @Override
    public String toString() {
        return name + " (" + department + ")";
    }
}
