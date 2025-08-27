package com.group.campus.activities;

import android.app.KeyguardManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.group.campus.R;
import com.group.campus.managers.ReminderManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderAlertActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;
    private String reminderId;
    private String eventTitle;
    private long eventStartTime;

    private TextView tvEventTitle;
    private TextView tvEventTime;
    private TextView tvReminderMessage;
    private Button btnSnooze;
    private Button btnDismiss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure this activity shows over lock screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setContentView(R.layout.activity_reminder_alert);

        // Get reminder data from intent
        reminderId = getIntent().getStringExtra("reminder_id");
        eventTitle = getIntent().getStringExtra("event_title");
        eventStartTime = getIntent().getLongExtra("event_start_time", 0);

        initViews();
        setupEventDetails();
        setupButtons();
        startAlarmSound();
        acquireWakeLock();
    }

    private void initViews() {
        tvEventTitle = findViewById(R.id.tv_event_title);
        tvEventTime = findViewById(R.id.tv_event_time);
        tvReminderMessage = findViewById(R.id.tv_reminder_message);
        btnSnooze = findViewById(R.id.btn_snooze);
        btnDismiss = findViewById(R.id.btn_dismiss);
    }

    private void setupEventDetails() {
        tvEventTitle.setText(eventTitle);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Date startTime = new Date(eventStartTime);

        tvEventTime.setText(timeFormat.format(startTime));
        tvReminderMessage.setText("Event starts on " + dateFormat.format(startTime));
    }

    private void setupButtons() {
        btnSnooze.setOnClickListener(v -> {
            stopAlarmSound();
            snoozeReminder();
            finish();
        });

        btnDismiss.setOnClickListener(v -> {
            stopAlarmSound();
            dismissReminder();
            finish();
        });
    }

    private void startAlarmSound() {
        try {
            // Start vibration
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                long[] pattern = {0, 1000, 1000}; // Wait 0ms, vibrate 1000ms, wait 1000ms
                vibrator.vibrate(pattern, 0); // Repeat the pattern
            }

            // Start alarm sound
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, alarmSound);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAlarmSound() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private void snoozeReminder() {
        ReminderManager.getInstance().snoozeReminder(reminderId, 5); // Snooze for 5 minutes
    }

    private void dismissReminder() {
        ReminderManager.getInstance().dismissReminder(reminderId);
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "Campus:ReminderAlertWakeLock"
        );
        wakeLock.acquire(10 * 60 * 1000L); // 10 minutes
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarmSound();

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent back button from dismissing the alarm
        // User must use snooze or dismiss buttons
    }
}
