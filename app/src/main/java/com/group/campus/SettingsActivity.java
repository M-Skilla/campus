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

import com.group.campus.fragments.ChangePasswordFragment;

public class SettingsActivity extends AppCompatActivity {

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
                launchChangePasswordFragment();
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