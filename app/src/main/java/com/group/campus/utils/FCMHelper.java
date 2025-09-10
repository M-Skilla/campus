package com.group.campus.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.group.campus.service.FBMessagingService;

import java.util.HashMap;
import java.util.Map;

public class FCMHelper {

    private static final String TAG = "FCMHelper";
    public interface TokenCallback {
        void onTokenReceived(String token);
    }

    public static void sendTokenToServer(String token) {
        Map<String, Object> deviceToken = new HashMap<>();
        deviceToken.put("token", token);
        deviceToken.put("timestamp", FieldValue.serverTimestamp());

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        if (fAuth.getUid() != null) {
            FirebaseFirestore.getInstance().collection("fcmTokens")
                    .document(fAuth.getUid())
                    .set(deviceToken);
        } else {
            return;
        }
    }

    public static void getNewToken(TokenCallback callback) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "onComplete: Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        FirebaseAuth fAuth = FirebaseAuth.getInstance();
                        if (fAuth.getUid() != null) {
                            FirebaseFirestore.getInstance().collection("fcmTokens").document(fAuth.getUid()).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.getResult().exists()) {
                                                return;
                                            } else {
                                                sendTokenToServer(token);
                                            }
                                        }
                                    });

                        }
                        callback.onTokenReceived(token);
                        Log.d(TAG, "Token received: " + token);
                    }
                });

    }

    public static void manageNewAnnouncementsSubscription(boolean subscribe) {
        if (subscribe) {
            FirebaseMessaging.getInstance().subscribeToTopic("new_announcements")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Subscribed to New Announcements");
                        } else {
                            Log.w(TAG, "Failed to subscribe to New Announcements", task.getException());
                        }
                    });
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("new_announcements")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Unsubscribed from New Announcements");
                        } else {
                            Log.w(TAG, "Failed to unsubscribe from New Announcements", task.getException());
                        }
                    });
        }
    }
}
