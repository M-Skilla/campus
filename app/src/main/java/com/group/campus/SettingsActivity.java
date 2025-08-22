package com.group.campus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.group.campus.fragments.ChangePasswordFragment;

public class SettingsActivity extends AppCompatActivity {

    // Constants for SharedPreferences
    private static final String PREFS_NAME = "CampusSettings";
    private static final String KEY_THEME = "theme_mode";

    private MaterialButton backButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingsActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initializeViews();
        setupClickListeners();
        setupBackPressedCallback();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.settings_back_arrow);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(view -> finish());

        findViewById(R.id.item_change_password).setOnClickListener(view -> launchChangePasswordFragment());

        findViewById(R.id.item_theme_appearance).setOnClickListener(view -> showThemeDialog());

        findViewById(R.id.item_language).setOnClickListener(view ->
                Toast.makeText(SettingsActivity.this, "Language was clicked", Toast.LENGTH_SHORT).show());
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    findViewById(R.id.settingsActivity).setVisibility(View.VISIBLE);
                    findViewById(R.id.fragment_container).setVisibility(View.GONE);
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });
    }

    private void launchChangePasswordFragment() {
        ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        findViewById(R.id.settingsActivity).setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

        fragmentTransaction.replace(R.id.fragment_container, changePasswordFragment);
        fragmentTransaction.addToBackStack("ChangePasswordFragment");
        fragmentTransaction.commit();
    }

    private void showThemeDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_theme_selection, null);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.PopupDialog)
                .setView(dialogView)
                .create();

        int currentMode = getCurrentThemeMode();
        showCurrentSelection(dialogView, currentMode);

        dialogView.findViewById(R.id.option_match_phone).setOnClickListener(v -> {
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            saveTheme(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
            dialog.dismiss();
            recreate();
        });

        dialogView.findViewById(R.id.option_on).setOnClickListener(v -> {
            int mode = AppCompatDelegate.MODE_NIGHT_YES;
            saveTheme(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
            dialog.dismiss();
            recreate();
        });

        dialogView.findViewById(R.id.option_off).setOnClickListener(v -> {
            int mode = AppCompatDelegate.MODE_NIGHT_NO;
            saveTheme(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
            dialog.dismiss();
            recreate();
        });

        dialog.show();
    }

    private void showCurrentSelection(View dialogView, int currentMode) {
        dialogView.findViewById(R.id.tick_match_phone).setVisibility(View.GONE);
        dialogView.findViewById(R.id.tick_on).setVisibility(View.GONE);
        dialogView.findViewById(R.id.tick_off).setVisibility(View.GONE);

        switch (currentMode) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                dialogView.findViewById(R.id.tick_match_phone).setVisibility(View.VISIBLE);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                dialogView.findViewById(R.id.tick_on).setVisibility(View.VISIBLE);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                dialogView.findViewById(R.id.tick_off).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void saveTheme(int mode) {
        sharedPreferences.edit().putInt(KEY_THEME, mode).apply();
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int mode = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private int getCurrentThemeMode() {
        return sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_NO);
    }
}