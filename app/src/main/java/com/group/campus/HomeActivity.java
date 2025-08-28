package com.group.campus;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.group.campus.fragments.AnnouncementFragment;
import com.group.campus.fragments.CalendarFragment;
import com.group.campus.fragments.ProfileFragment;
import com.group.campus.ui.suggestions.SuggestionsFragment;
import com.group.campus.utils.PreferenceManager;

public class HomeActivity extends AppCompatActivity {

    private PreferenceManager prefs;

    private FrameLayout bottomNav;

    private FloatingActionButton fab;

    private Button button;
    CustomBottomNavView bottomNavView;
    private int previousItemId;

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


        View navView = findViewById(R.id.customBottomNav);
        FragmentManager fm = getSupportFragmentManager();
        bottomNavView = new CustomBottomNavView(navView, fm, R.id.container);

        bottomNavView.selectTab(0);

//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.container, new AnnouncementFragment())
//                .commit();
//        bottomNav.setSelectedItemId(R.id.announcementsItem);
//
//        bottomNav.setOnItemSelectedListener(item -> {
//            Fragment selectedFragment = null;
//
//            if (item.getItemId() == R.id.announcementsItem) {
//                selectedFragment = new AnnouncementFragment();
//            } else if (item.getItemId() == R.id.calendarItem) {
//                selectedFragment = new CalendarFragment();
//            } else if (item.getItemId() == R.id.profileItem) {
//                selectedFragment = new ProfileFragment();
//            } else if (item.getItemId() == R.id.suggestionsItem) {
//                selectedFragment = new SuggestionsFragment();
//            }
//            else if (item.getItemId() == R.id.fragment_container) {
//                selectedFragment = new CalendarFragment();
//            }
//
//
//
//
//            if (selectedFragment != null) {
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.container, selectedFragment)
//                        .commit();
//                return true;
//            }
//
//            return false;
//        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Fragment prevFragment = getSupportFragmentManager().findFragmentById(previousItemId);
//        if (prevFragment != null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.container, prevFragment)
//                    .commit();
//            bottomNav.setSelectedItemId(previousItemId);
//        } else {
//            getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.container, new AnnouncementFragment())
//                .commit();
//            bottomNav.setSelectedItemId(R.id.announcementsItem);
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        previousItemId = bottomNav.getSelectedItemId();
//
//    }


    public CustomBottomNavView getCustomBottomNavView() {
        return bottomNavView;
    }
}