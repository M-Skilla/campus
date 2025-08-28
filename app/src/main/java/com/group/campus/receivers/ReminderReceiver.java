package com.group.campus.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;

import com.group.campus.activities.ReminderAlertActivity;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        String eventTitle = intent.getStringExtra("event_title");
        long eventStartTime = intent.getLongExtra("event_start_time", 0);

        // Wake up the device
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "Campus:ReminderWakeLock"
        );
        wakeLock.acquire(10 * 60 * 1000L); // 10 minutes

        // Start the reminder alert activity
        Intent alertIntent = new Intent(context, ReminderAlertActivity.class);
        alertIntent.putExtra("reminder_id", reminderId);
        alertIntent.putExtra("event_title", eventTitle);
        alertIntent.putExtra("event_start_time", eventStartTime);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(alertIntent);

        // Release wake lock after a short delay (the activity will handle keeping device awake)
        wakeLock.release();
    }
}
