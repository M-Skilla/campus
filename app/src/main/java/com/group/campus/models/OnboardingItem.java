package com.group.campus.models;

public class OnboardingItem {
    private int image;

    private String header, title, description;

    public OnboardingItem(int image, String header, String title, String description) {
        this.image = image;
        this.header = header;
        this.title = title;
        this.description = description;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
