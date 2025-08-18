package com.group.campus.models;



public class AuthUser {
    private String id;

    private String aud;

    private String role;

    private String email;

    public AuthUser() {
    }

    public AuthUser(String id, String aud, String role, String email) {
        this.id = id;
        this.aud = aud;
        this.role = role;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
