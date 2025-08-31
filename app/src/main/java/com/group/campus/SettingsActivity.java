package com.group.campus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.firebase.auth.FirebaseAuth;
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
        applyTheme(this);
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
                Toast.makeText(SettingsActivity.this, "Changing language comming soon", Toast.LENGTH_SHORT).show());

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

        // Apply the same wider dialog styling as logout dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.LogoutDialogAnimation;
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // Make dialog wider - same as logout dialog
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

}