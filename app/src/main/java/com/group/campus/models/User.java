package com.group.campus.models;

import com.google.gson.annotations.SerializedName;

public class User {

    private String id, regNo, fullName;

    private College college;

    private Programme programme;

    private Role roles;

    @SerializedName("created_at")
    private String createdAt;

    public User() {
    }

    public User(String id, String regNo, String fullName, College college, Programme programme, Role roles, String createdAt) {
        this.id = id;
        this.regNo = regNo;
        this.fullName = fullName;
        this.college = college;
        this.programme = programme;
        this.roles = roles;
        this.createdAt = createdAt;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
