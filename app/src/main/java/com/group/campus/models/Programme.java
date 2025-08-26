package com.group.campus.models;

import com.google.gson.annotations.SerializedName;

public class Programme {

    private Long id;
    private String name, abbrv;
    private College college;
    private Year year;

    @SerializedName("created_at")
    private String createdAt;

    public Programme() {}

    public Programme(Long id, String name, String abbrv, College college, Year year, String createdAt) {
        this.id = id;
        this.name = name;
        this.abbrv = abbrv;
        this.college = college;
        this.year = year;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAbbrv() { return abbrv; }
    public void setAbbrv(String abbrv) { this.abbrv = abbrv; }

    public College getCollege() { return college; }
    public void setCollege(College college) { this.college = college; }

    public Year getYear() { return year; }
    public void setYear(Year year) { this.year = year; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
