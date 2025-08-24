package com.group.campus.models;

import java.util.List;

public class Author {

    private String id, name;

    private List<String> roles;

    private College college;

    private String profilePictureUrls;

    public Author() {
    }

    public Author(String id, String name, List<String> roles, College college, String profilePictureUrls) {
        this.id = id;
        this.name = name;
        this.roles = roles;
        this.college = college;
        this.profilePictureUrls = profilePictureUrls;
    }

    public String getProfilePictureUrls() {
        return profilePictureUrls;
    }

    public void setProfilePictureUrls(String profilePictureUrls) {
        this.profilePictureUrls = profilePictureUrls;
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
}
