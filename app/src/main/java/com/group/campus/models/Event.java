package com.group.campus.models;

import java.text.SimpleDateFormat;

import java.text.ParseException;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Event {
    private String id;
    private String title;
    private Date startDate;
    private Date endDate;
    private boolean isAllDay;
    private boolean hasSoundAlert;
    private String description;


    // Firestore date format
    private static final SimpleDateFormat FIRESTORE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


    public Event() {
        // Default constructor
    }

    public Event(String title, Date startDate, Date endDate, boolean isAllDay, boolean hasSoundAlert) {
        this.id = generateId();
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAllDay = isAllDay;
        this.hasSoundAlert = hasSoundAlert;
    }


    // Constructor for Firestore data (with String dates)
    public Event(String title, String startDate, String endDate) {
        this.title = title;
        try {
            this.startDate = FIRESTORE_DATE_FORMAT.parse(startDate);
            this.endDate = FIRESTORE_DATE_FORMAT.parse(endDate);
        } catch (ParseException e) {
            // Fallback to current date if parsing fails
            this.startDate = new Date();
            this.endDate = new Date();
        }
        this.isAllDay = false;
        this.hasSoundAlert = false;
    }


    private String generateId() {
        return "event_" + System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public boolean hasSoundAlert() {
        return hasSoundAlert;
    }

    public void setHasSoundAlert(boolean hasSoundAlert) {
        this.hasSoundAlert = hasSoundAlert;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormattedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        return dateFormat.format(startDate);
    }

    public String getFormattedTime() {
        if (isAllDay) {
            return "All day";
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(startDate) + " - " + timeFormat.format(endDate);
    }

    public boolean isOnDate(int year, int month, int day) {
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTime(startDate);

        return eventCal.get(Calendar.YEAR) == year &&
               eventCal.get(Calendar.MONTH) == month &&
               eventCal.get(Calendar.DAY_OF_MONTH) == day;
    }

    public int getDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        return cal.get(Calendar.MONTH);
    }

    public int getYear() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        return cal.get(Calendar.YEAR);
    }


    // Methods to get String dates for Firestore
    public String getStartDateString() {
        return startDate != null ? FIRESTORE_DATE_FORMAT.format(startDate) : "";
    }

    public String getEndDateString() {
        return endDate != null ? FIRESTORE_DATE_FORMAT.format(endDate) : "";
    }

}
