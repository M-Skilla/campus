package com.group.campus.models;

import java.util.Date;
import java.util.List;

public class Announcement {
    private String body, title, department;

    private Date createdAt;

    private List<String> visibility;

    private Author author;

    public Announcement() {
    }

    public Announcement(String body, String title, Date createdAt, List<String> visibility, Author author, String department) {
        this.body = body;
        this.title = title;
        this.createdAt = createdAt;
        this.visibility = visibility;
        this.author = author;
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getVisibility() {
        return visibility;
    }

    public void setVisibility(List<String> visibility) {
        this.visibility = visibility;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
