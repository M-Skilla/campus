package com.group.campus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.view.View;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "app_theme";

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

        ImageButton backButton = findViewById(R.id.settings_back_arrow);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        View changePasswordItem = findViewById(R.id.item_change_password);
        changePasswordItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Change Password was clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View manageDevicesItem = findViewById(R.id.item_manage_devices);
        manageDevicesItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Manage Devices was clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View themeAppearanceItem = findViewById(R.id.item_theme_appearance);
        themeAppearanceItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showThemeDialog();
            }
        });

        View languageItem = findViewById(R.id.item_language);
        languageItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Language was clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View dataPolicyItem = findViewById(R.id.item_data_policy);
        dataPolicyItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Data Policy was clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View faqItem = findViewById(R.id.item_faq);
        faqItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Frequently Asked Questions was clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View contactSupportItem = findViewById(R.id.item_contact_support);
        contactSupportItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Contact Support was clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View reportBugItem = findViewById(R.id.item_report_bug);
        reportBugItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Report Bug was clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View sendFeedbackItem = findViewById(R.id.item_send_feedback);
        sendFeedbackItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Send Feedback was clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View licenseItem = findViewById(R.id.item_licenses);
        licenseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "License was clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View acknowledgementsItem = findViewById(R.id.item_acknowledgements);
        acknowledgementsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Acknowledgments was clicked", Toast.LENGTH_SHORT).show();
            }
        });
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
        // Hide all ticks first
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
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, mode).apply();
    }

    private void applySavedTheme(){
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int mode = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private int getCurrentThemeMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_NO);
    }


}