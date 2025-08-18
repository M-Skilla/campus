package com.group.campus.utils;

import java.util.Date;

public class DateUtils {

    /**
     * Returns a human-readable string representing the time passed since the given date
     * @param date The date to compare with the current time
     * @return A string like "2 hours ago", "5 minutes ago", "1 day ago", etc.
     */
    public static String getTimeAgo(Date date) {
        if (date == null) {
            return "Unknown";
        }

        long currentTime = System.currentTimeMillis();
        long givenTime = date.getTime();
        long timeDifference = currentTime - givenTime;

        // Convert to seconds
        long seconds = timeDifference / 1000;

        if (seconds < 60) {
            return seconds <= 1 ? "Just now" : seconds + " seconds ago";
        }

        // Convert to minutes
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        }

        // Convert to hours
        long hours = minutes / 60;
        if (hours < 24) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        }

        // Convert to days
        long days = hours / 24;
        if (days < 7) {
            return days == 1 ? "1 day ago" : days + " days ago";
        }

        // Convert to weeks
        long weeks = days / 7;
        if (weeks < 4) {
            return weeks == 1 ? "1 week ago" : weeks + " weeks ago";
        }

        // Convert to months (approximate)
        long months = days / 30;
        if (months < 12) {
            return months == 1 ? "1 month ago" : months + " months ago";
        }

        // Convert to years
        long years = days / 365;
        return years == 1 ? "1 year ago" : years + " years ago";
    }
}
