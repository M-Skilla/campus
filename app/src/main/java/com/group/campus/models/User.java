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

    public void setId(String id) {
        this.id = id;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
    }

    public Programme getProgramme() {
        return programme;
    }

    public void setProgramme(Programme programme) {
        this.programme = programme;
    }

    public Role getRoles() {
        return roles;
    }

    public void setRoles(Role roles) {
        this.roles = roles;
    }

}
