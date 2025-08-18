package com.group.campus;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.group.campus.adapters.OnboardingAdapter;
import com.group.campus.models.OnboardingItem;
import com.group.campus.utils.PreferenceManager;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;

    private WormDotsIndicator wormDotsIndicator;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preferenceManager = new PreferenceManager(this);
        viewPager2 = findViewById(R.id.viewPagerOnboarding);
        wormDotsIndicator = findViewById(R.id.dotsIndicator);

        List<OnboardingItem> onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(
                R.drawable.onboarding,
                "Stay Updated",
                "Campus Announcements",
                "All your university news in one place."
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.onboarding,
                "Plan Your Days",
                "Events & Calendar",
                "Never miss a class or a campus event."
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.onboarding,
                "Speak Up",
                "Suggestions & Ideas",
                "Share feedback that makes a difference."
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.onboarding,
                "Start Connecting",
                "Student Marketplace",
                "Buy, sell, and swap with fellow students."
        ));

        viewPager2.setAdapter(new OnboardingAdapter(onboardingItems, v -> {
            // Mark onboarding as complete
            preferenceManager.setFirstTimeComplete();

            // Navigate to login activity
            startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
            finish(); // Prevent going back to onboarding
        }));

        wormDotsIndicator.attachTo(viewPager2);
    }
}