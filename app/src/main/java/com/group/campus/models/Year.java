package com.group.campus.models;

import com.google.gson.annotations.SerializedName;

public class Year {

    private Integer id;

    private Integer numeric;

    @SerializedName("created_at")
    private String createdAt;

    public Year() {
    }

    public Year(Integer id, Integer numeric, String createdAt) {
        this.id = id;
        this.numeric = numeric;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumeric() {
        return numeric;
    }

    public void setNumeric(Integer numeric) {
        this.numeric = numeric;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
