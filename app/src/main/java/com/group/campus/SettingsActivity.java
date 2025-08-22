package com.group.campus;

import android.os.Bundle;
import android.widget.ImageButton;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
                Toast.makeText(SettingsActivity.this, "Theme Appearance was clicked", Toast.LENGTH_SHORT).show();
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
}