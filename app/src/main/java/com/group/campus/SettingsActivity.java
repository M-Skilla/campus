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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.group.campus.fragments.ChangePasswordFragment;

public class SettingsActivity extends AppCompatActivity {


    private MaterialButton backButton;


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

        backButton = findViewById(R.id.settings_back_arrow);
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
                launchChangePasswordFragment();
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

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            findViewById(R.id.settingsActivity).setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_container).setVisibility(View.GONE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
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