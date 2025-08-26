package com.group.campus;

import android.content.Intent;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group.campus.fragments.ProfileFragment;
import com.group.campus.utils.PreferenceManager;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        TextView tvHello = findViewById(R.id.tvHello);

        tvHello.setOnClickListener(v -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main, new ProfileFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            Toast.makeText(this, "User Logged In", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
        }
    }
}
