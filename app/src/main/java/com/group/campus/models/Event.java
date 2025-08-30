package com.group.campus.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.Calendar;

public class Event {
    private String id;
    private String title;
    private Date startDate;
    private Date endDate;
    private boolean isAllDay;
    private boolean hasSoundAlert;
    private String startDateString;
    private String endDateString;

    // Default constructor for Firebase
    public Event() {
        this.id = UUID.randomUUID().toString();
    }

    // Constructor with Date objects
    public Event(String title, Date startDate, Date endDate, boolean isAllDay, boolean hasSoundAlert) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAllDay = isAllDay;
        this.hasSoundAlert = hasSoundAlert;
        updateDateStrings();
    }

    // Constructor with String dates (for Firebase parsing)
    public Event(String title, String startDate, String endDate) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.startDateString = startDate;
        this.endDateString = endDate;
        this.isAllDay = false;
        this.hasSoundAlert = false;
        parseDateStrings();
    }

    private void updateDateStrings() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        if (startDate != null) {
            startDateString = format.format(startDate);
        }
        if (endDate != null) {
            endDateString = format.format(endDate);
        }
    }

    private void parseDateStrings() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            if (startDateString != null && !startDateString.isEmpty()) {
                startDate = format.parse(startDateString);
            }
            if (endDateString != null && !endDateString.isEmpty()) {
                endDate = format.parse(endDateString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        updateDateStrings();
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
        updateDateStrings();
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

    public String getStartDateString() {
        if (startDateString == null && startDate != null) {
            updateDateStrings();
        }
        return startDateString;
    }

    public void setStartDateString(String startDateString) {
        this.startDateString = startDateString;
        parseDateStrings();
    }

    public String getEndDateString() {
        if (endDateString == null && endDate != null) {
            updateDateStrings();
        }
        return endDateString;
    }

    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
        parseDateStrings();
    }

    public String getFormattedDate() {
        if (startDate != null) {
            SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            return format.format(startDate);
        }
        return "";
    }

    public String getFormattedTime() {
        if (startDate != null) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return format.format(startDate);
        }
        return "";
    }

    public boolean isOnDate(int year, int month, int day) {
        if (startDate == null) {
            return false;
        }

        Calendar eventCalendar = Calendar.getInstance();
        eventCalendar.setTime(startDate);

        return eventCalendar.get(Calendar.YEAR) == year &&
               eventCalendar.get(Calendar.MONTH) == month &&
               eventCalendar.get(Calendar.DAY_OF_MONTH) == day;
    }
}
