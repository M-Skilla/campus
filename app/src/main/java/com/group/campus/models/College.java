package com.group.campus.models;


public class College {
    private String id;

    private String name, abbrv;

    private Programme programme;

    public College() {
    }

    public College(String id, String name, String abbrv, Programme programme) {
        this.id = id;
        this.name = name;
        this.abbrv = abbrv;
        this.programme = programme;
    }

    public Programme getProgramme() {
        return programme;
    }

    public void setProgramme(Programme programme) {
        this.programme = programme;
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
