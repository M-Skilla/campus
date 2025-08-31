package com.group.campus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.group.campus.service.FBMessagingService;
import com.group.campus.ui.suggestions.SuggestionsFragment;
import com.group.campus.utils.FCMHelper;
import com.group.campus.utils.PreferenceManager;

public class HomeActivity extends AppCompatActivity {

    private PreferenceManager prefs;

    private FrameLayout bottomNav;

    private FloatingActionButton fab;

    private Button button;
    CustomBottomNavView bottomNavView;
    private int previousItemId;

    private final String TAG = "HomeActivity";
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            Toast.makeText(this, "Can post notifications", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "The app won't show notifications", Toast.LENGTH_SHORT).show();
        }
    });

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

        askNotificationPermission();

        bottomNavView.selectTab(0);
        Log.i(TAG, "onCreate: Is this working!");

        FCMHelper.getNewToken(token -> {
            Log.i(TAG, "onCreate: Existing Token -> " + token);
        });


    }

    protected void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Can post notifications", Toast.LENGTH_SHORT).show();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(this, "Allow notification from settings", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    public CustomBottomNavView getCustomBottomNavView() {
        return bottomNavView;
    }
}