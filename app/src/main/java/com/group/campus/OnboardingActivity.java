package com.group.campus;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.group.campus.adapters.OnboardingAdapter;
import com.group.campus.models.OnboardingItem;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;

    private WormDotsIndicator wormDotsIndicator;
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

        viewPager2 = findViewById(R.id.viewPagerOnboarding);
        wormDotsIndicator = findViewById(R.id.dotsIndicator);

        List<OnboardingItem> onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_launcher_background,
                "Stay Updated",
                "Campus Announcements",
                "All your university news in one place."
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_launcher_background,
                "Plan Your Days",
                "Events & Calendar",
                "Never miss a class or a campus event."
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_launcher_background,
                "Speak Up",
                "Suggestions & Ideas",
                "Share feedback that makes a difference."
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_launcher_background,
                "Start Connecting",
                "Student Marketplace",
                "Buy, sell, and swap with fellow students."
        ));

        viewPager2.setAdapter(new OnboardingAdapter(onboardingItems));

        wormDotsIndicator.attachTo(viewPager2);
    }
}