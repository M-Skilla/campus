package com.group.campus.models;

import java.util.Date;

public class Reminder {
    private String id;
    private String eventTitle;
    private Date eventStartTime;
    private Date reminderTime;
    private int reminderMinutesBefore;
    private boolean isActive;

    public Reminder() {
        // Default constructor for Firebase
    }

    public Reminder(String id, String eventTitle, Date eventStartTime, int reminderMinutesBefore) {
        this.id = id;
        this.eventTitle = eventTitle;
        this.eventStartTime = eventStartTime;
        this.reminderMinutesBefore = reminderMinutesBefore;
        this.isActive = true;

        // Calculate reminder time
        long reminderTimeMillis = eventStartTime.getTime() - (reminderMinutesBefore * 60 * 1000L);
        this.reminderTime = new Date(reminderTimeMillis);
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public Date getEventStartTime() { return eventStartTime; }
    public void setEventStartTime(Date eventStartTime) { this.eventStartTime = eventStartTime; }

    public Date getReminderTime() { return reminderTime; }
    public void setReminderTime(Date reminderTime) { this.reminderTime = reminderTime; }

    public int getReminderMinutesBefore() { return reminderMinutesBefore; }
    public void setReminderMinutesBefore(int reminderMinutesBefore) { this.reminderMinutesBefore = reminderMinutesBefore; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getReminderTimeText() {
        if (reminderMinutesBefore < 60) {
            return reminderMinutesBefore + " minutes before";
        } else if (reminderMinutesBefore < 1440) {
            int hours = reminderMinutesBefore / 60;
            return hours + " hour" + (hours > 1 ? "s" : "") + " before";
        } else {
            int days = reminderMinutesBefore / 1440;
            return days + " day" + (days > 1 ? "s" : "") + " before";
        }
    }
}
