package com.group.campus.models;

public class Programme {

    private String id;
    private String name, abbrv;
    private Year year;


    public Programme() {}

    public Programme(String id, String name, String abbrv, Year year) {
        this.id = id;
        this.name = name;
        this.abbrv = abbrv;
        this.year = year;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAbbrv() { return abbrv; }
    public void setAbbrv(String abbrv) { this.abbrv = abbrv; }


    public Year getYear() { return year; }
    public void setYear(Year year) { this.year = year; }


}
