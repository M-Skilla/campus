package com.group.campus.models;

import com.google.gson.annotations.SerializedName;

public class Role {
    private String name;

    private Long id;

    @SerializedName("created_at")
    private String createdAt;

    public Role(String name, Long id, String createdAt) {
        this.name = name;
        this.id = id;
        this.createdAt = createdAt;
    }

    public Role() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
