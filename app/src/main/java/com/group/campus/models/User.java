package com.group.campus.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class User {

    private String id, regNo, fullName, profilePicUrl;

    private College college;

    private Programme programme;

    private Role roles;

    private Date startDate, endDate;


    public User() {
        // Default constructor required for Firebase
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public User(String id, String regNo, String fullName, String profilePicUrl, College college, Programme programme, Role roles, Date startDate, Date endDate) {
        this.id = id;
        this.regNo = regNo;
        this.fullName = fullName;
        this.profilePicUrl = profilePicUrl;
        this.college = college;
        this.programme = programme;
        this.roles = roles;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() {
        return id;

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



}
