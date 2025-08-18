package com.group.campus.models;

import com.google.gson.annotations.SerializedName;

public class College {
    private String id;

    private String name, abbrv;


    public College() {
    }

    public College(String id, String name, String abbrv) {
        this.id = id;
        this.name = name;
        this.abbrv = abbrv;
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

    public String getAbbrv() {
        return abbrv;
    }

    public void setAbbrv(String abbrv) {
        this.abbrv = abbrv;
    }


}
