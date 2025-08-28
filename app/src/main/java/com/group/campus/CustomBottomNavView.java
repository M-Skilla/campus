package com.group.campus;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.group.campus.fragments.AnnouncementFragment;
import com.group.campus.fragments.CalendarFragment;
import com.group.campus.fragments.ProfileFragment;
import com.group.campus.ui.suggestions.SuggestionsFragment;

public class CustomBottomNavView {

    private final View navView;
    private final FragmentManager fragmentManager;
    private final int containerId;
    private int selectedTab = -1;

    public CustomBottomNavView(View navView, FragmentManager fragmentManager, int containerId) {
        this.navView = navView;
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
        setupListeners();
    }

    private void setupListeners() {
        navView.findViewById(R.id.news_item).setOnClickListener(v -> selectTab(0));
        navView.findViewById(R.id.suggestions_item).setOnClickListener(v -> selectTab(1));
        navView.findViewById(R.id.calendar_item).setOnClickListener(v -> selectTab(2));
        navView.findViewById(R.id.profile_item).setOnClickListener(v -> selectTab(3));
    }

    public void selectTab(int index) {
        if (selectedTab == index) return;
        resetAllTabs();
        selectedTab = index;

        switch (index) {
            case 0:
                setActive(R.id.news_item, R.id.tvNews);
                loadFragment(new AnnouncementFragment());
                break;
            case 1:
                setActive(R.id.suggestions_item, R.id.tvSuggestion);
                loadFragment(new SuggestionsFragment());
                break;
            case 2:
                setActive(R.id.calendar_item, R.id.tvCalendar);
                loadFragment(new CalendarFragment());
                break;
            case 3:
                setActive(R.id.profile_item, R.id.tvProfile);
                loadFragment(new ProfileFragment());
                break;

        }
    }

    private void resetAllTabs() {
        setInactive(R.id.news_item, R.id.tvNews);
        setInactive(R.id.suggestions_item, R.id.tvSuggestion);
        setInactive(R.id.calendar_item, R.id.tvCalendar);
        setInactive(R.id.profile_item, R.id.tvProfile);

    }

    private void setActive(@IdRes int itemContainerId, @IdRes int textId) {
        LinearLayout container = navView.findViewById(itemContainerId);
        TextView textView = navView.findViewById(textId);

        // Animate background
        container.setBackground(ContextCompat.getDrawable(navView.getContext(), R.drawable.bg_item_nav));
//        container.setAlpha(0f);
//        container.animate().alpha(1f).setDuration(200).start();

        // Animate text appearance
//        textView.setAlpha(0f);
        textView.setVisibility(View.VISIBLE);
//        textView.animate().alpha(1f).setDuration(200).start();
    }


    private void setInactive(@IdRes int itemContainerId, @IdRes int textId) {
        ((LinearLayout) navView.findViewById(itemContainerId)).setBackground(null);
        ((TextView) navView.findViewById(textId))
                .setVisibility(GONE);
    }

    private void loadFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }
}
