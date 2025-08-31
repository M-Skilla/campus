package com.group.campus.models;

import androidx.annotation.NonNull;

import java.util.List;

public class Author {

    private String id, name;

    private List<String> roles;

    private College college;

    private String profilePicUrl;

    public Author() {
    }

    public Author(String id, String name, List<String> roles, College college, String profilePicUrl) {
        this.id = id;
        this.name = name;
        this.roles = roles;
        this.college = college;
        this.profilePicUrl = profilePicUrl;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
    }

    @NonNull
    @Override
    public String toString() {
        return "Author{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", roles=" + roles +
                ", college=" + college +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                '}';
    }
}
