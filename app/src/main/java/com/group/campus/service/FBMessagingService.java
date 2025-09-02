package com.group.campus.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.group.campus.MainActivity;
import com.group.campus.R;
import com.group.campus.utils.FCMHelper;

import java.util.Random;

public class FBMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FBMessagingService";
    @Override
    public void onNewToken(@NonNull String token) {
        Log.i(TAG, "onNewToken: Token -> " + token);
        FCMHelper.sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = message.getData().get("title");
        String body = message.getData().get("body");
        String screen = message.getData().get("screen");
        String announcementId = message.getData().get("announcementId");

        sendNotification(title, body, screen != null ? screen : "", announcementId);
    }

    private void sendNotification(String title, String body, String screen, String announcementId) {
        Intent intent;

        if (screen.equals("AnnouncementViewActivity")) {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("screen", screen);
            intent.putExtra("announcementId", announcementId);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default_channel")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                "default_channel",
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(channel);

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }
}

