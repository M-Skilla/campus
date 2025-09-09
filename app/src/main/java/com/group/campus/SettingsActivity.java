package com.group.campus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.group.campus.utils.FCMHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.button.MaterialButton;
import com.group.campus.fragments.ChangePasswordFragment;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    // Constants for SharedPreferences
    private static final String PREFS_NAME = "CampusSettings";
    private static final String KEY_THEME = "theme_mode";

    private MaterialButton backButton;
    private SharedPreferences sharedPreferences;

    private SwitchCompat switchNewAnnouncements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Initialize sharedPreferences with the consistent PREFS_NAME
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        switchNewAnnouncements = findViewById(R.id.switch_new_announcements);

        // Retrieve the saved state of the switch, defaulting to true
        boolean isSubscribed = sharedPreferences.getBoolean("new_announcements", true);
        switchNewAnnouncements.setChecked(isSubscribed);

        switchNewAnnouncements.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Use the correct method to save the state
            saveSwitchState("new_announcements", isChecked);
            FCMHelper.manageNewAnnouncementsSubscription(isChecked);
        });

        TextView currentLanguage = findViewById(R.id.current_language);
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String lang = prefs.getString("app_lang", "en");
        if ("sw".equals(lang)) {
            currentLanguage.setText("Swahili");
        } else {
            currentLanguage.setText("English");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingsActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
        setupBackPressedCallback();
    }

    private void saveSwitchState(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.settings_back_arrow);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(view -> finish());

        findViewById(R.id.item_change_password).setOnClickListener(view -> launchChangePasswordFragment());

        findViewById(R.id.item_theme_appearance).setOnClickListener(view -> showThemeDialog());

        findViewById(R.id.item_language).setOnClickListener(view ->  showLanguageDialog());

        findViewById(R.id.item_logout).setOnClickListener(view -> showLogoutDialog());
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
                .setCancelable(true)
                .create();

        int currentMode = getCurrentThemeMode();
        showCurrentSelection(dialogView, currentMode);

        dialogView.findViewById(R.id.option_match_phone).setOnClickListener(v -> {
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (currentMode != mode) {
                saveTheme(mode);
                dialog.dismiss();
                recreate();
            } else {
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.option_on).setOnClickListener(v -> {
            int mode = AppCompatDelegate.MODE_NIGHT_YES;
            if (currentMode != mode) {
                saveTheme(mode);
                dialog.dismiss();
                recreate();
            } else {
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.option_off).setOnClickListener(v -> {
            int mode = AppCompatDelegate.MODE_NIGHT_NO;
            if (currentMode != mode) {
                saveTheme(mode);
                dialog.dismiss();
                recreate();
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.LogoutDialogAnimation;
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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

    private int getCurrentThemeMode() {
        return sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    // Static method to apply theme across all activities
    public static void applyTheme(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int mode = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private void showLogoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout_confirmation, null);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.PopupDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            dialog.dismiss();
            performLogout();
        });

        dialog.show();

        // Set dialog width to 90% of screen width
        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.LogoutDialogAnimation;
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // Make dialog wider
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void performLogout() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
            fragmentManager.popBackStack();
        }

        if (fragmentManager.findFragmentById(R.id.fragment_container) != null) {
            fragmentManager.beginTransaction()
                    .remove(fragmentManager.findFragmentById(R.id.fragment_container))
                    .commitNowAllowingStateLoss();
        }

        sharedPreferences.edit().clear().apply();

        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void showLanguageDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_language_selection, null);

        LinearLayout optionEnglish = dialogView.findViewById(R.id.option_english);
        LinearLayout optionSwahili = dialogView.findViewById(R.id.option_swahili);
        View tickEnglish = dialogView.findViewById(R.id.tick_english);
        View tickSwahili = dialogView.findViewById(R.id.tick_swahili);

        // Get saved language, default to "en"
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String lang = prefs.getString("app_lang", "en");

        // Show tick for current language
        if ("sw".equals(lang)) {
            tickEnglish.setVisibility(View.GONE);
            tickSwahili.setVisibility(View.VISIBLE);
        } else {
            tickEnglish.setVisibility(View.VISIBLE);
            tickSwahili.setVisibility(View.GONE);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        optionEnglish.setOnClickListener(v -> {
            tickEnglish.setVisibility(View.VISIBLE);
            tickSwahili.setVisibility(View.GONE);
            setLocale("en");
            dialog.dismiss();
        });

        optionSwahili.setOnClickListener(v -> {
            tickEnglish.setVisibility(View.GONE);
            tickSwahili.setVisibility(View.VISIBLE);
            setLocale("sw");
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.LogoutDialogAnimation;
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Save preference
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putString("app_lang", langCode).apply();

        // Restart activity to apply changes
        recreate();
    }

}