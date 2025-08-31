package com.group.campus;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.group.campus.fragments.ProfileFragment;
import com.group.campus.models.Announcement;
import com.group.campus.utils.PreferenceManager;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth fAuth;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvHello = findViewById(R.id.tvHello);
        tvHello.setOnClickListener(v -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main, new ProfileFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
        Intent intent = getIntent();

        handleIntent(intent);


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            Toast.makeText(this, "User Logged In", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String screen = intent.getStringExtra("screen");
        String announcementId = intent.getStringExtra("announcementId");

        if ("AnnouncementViewActivity".equals(screen) && announcementId != null) {
            FirebaseFirestore.getInstance()
                    .collection("announcement")
                    .document(announcementId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Announcement announcement = documentSnapshot.toObject(Announcement.class);
                            Intent anotherIntent = new Intent(this, AnnouncementViewActivity.class);
                            anotherIntent.putExtra("announcement", announcement);
                            startActivity(anotherIntent);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch announcement", e));
        } else {
            fAuth = FirebaseAuth.getInstance();
            preferenceManager = new PreferenceManager(this);

            if (preferenceManager.isFirstTime()) {
                startActivity(new Intent(MainActivity.this, OnboardingActivity.class));
                finish();
            } else {
                FirebaseUser user = fAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }
    }
}
