package com.group.campus;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.group.campus.fragments.AnnouncementFragment;
import com.group.campus.fragments.CalendarFragment;
import com.group.campus.fragments.ProfileFragment;
import com.group.campus.fragments.SuggestionsFragment;
import com.group.campus.utils.PreferenceManager;

public class HomeActivity extends AppCompatActivity {

    private PreferenceManager prefs;

    private BottomNavigationView bottomNav;

    private FloatingActionButton fab;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        bottomNav = findViewById(R.id.bottomNav);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new AnnouncementFragment())
                .commit();
        bottomNav.setSelectedItemId(R.id.announcementsItem);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.announcementsItem) {
                selectedFragment = new AnnouncementFragment();
            } else if (item.getItemId() == R.id.calendarItem) {
                selectedFragment = new CalendarFragment();
            } else if (item.getItemId() == R.id.profileItem) {
                selectedFragment = new ProfileFragment();
            } else if (item.getItemId() == R.id.suggestionsItem) {
                selectedFragment = new SuggestionsFragment();
            }
            else if (item.getItemId() == R.id.fragment_container) {
                selectedFragment = new CalendarFragment();
            }




            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });


//        prefs = new PreferenceManager(this);
//
//        button.setOnClickListener(v -> {
//            prefs.clearPreferences();
//            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new AnnouncementFragment())
                .commit();
        bottomNav.setSelectedItemId(R.id.announcementsItem);
    }
}