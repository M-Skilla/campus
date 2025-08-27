package com.group.campus.managers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.group.campus.models.Reminder;
import com.group.campus.receivers.ReminderReceiver;

import java.util.ArrayList;
import java.util.List;

public class ReminderManager {
    private static ReminderManager instance;
    private List<Reminder> reminders;
    private Context context;

    private ReminderManager() {
        reminders = new ArrayList<>();
    }

    public static ReminderManager getInstance() {
        if (instance == null) {
            instance = new ReminderManager();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public void addReminder(Reminder reminder) {
        reminders.add(reminder);
        scheduleAlarm(reminder);
    }

    public void removeReminder(String reminderId) {
        Reminder reminderToRemove = null;
        for (Reminder reminder : reminders) {
            if (reminder.getId().equals(reminderId)) {
                reminderToRemove = reminder;
                break;
            }
        }
        if (reminderToRemove != null) {
            reminders.remove(reminderToRemove);
            cancelAlarm(reminderToRemove);
        }
    }

    public List<Reminder> getAllReminders() {
        return new ArrayList<>(reminders);
    }

    public void snoozeReminder(String reminderId, int snoozeMinutes) {
        for (Reminder reminder : reminders) {
            if (reminder.getId().equals(reminderId)) {
                // Reschedule the reminder for snooze time
                long newReminderTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L);
                reminder.getReminderTime().setTime(newReminderTime);
                scheduleAlarm(reminder);
                break;
            }
        }
    }

    public void dismissReminder(String reminderId) {
        for (Reminder reminder : reminders) {
            if (reminder.getId().equals(reminderId)) {
                reminder.setActive(false);
                cancelAlarm(reminder);
                break;
            }
        }
    }

    private void scheduleAlarm(Reminder reminder) {
        if (context == null) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("event_title", reminder.getEventTitle());
        intent.putExtra("event_start_time", reminder.getEventStartTime().getTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = reminder.getReminderTime().getTime();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void cancelAlarm(Reminder reminder) {
        if (context == null) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}
