package com.group.campus.ui.suggestions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import com.group.campus.R;
import com.group.campus.service.UserRoleService;

public class SuggestionsFragment extends Fragment {


    private MaterialButton btnOutbox, btnInbox, btnStaffView;
    private MaterialCardView cardHealth, cardFacilities, cardLibrary;
    private UserRoleService userRoleService;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRoleService = new UserRoleService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggestions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {

        btnOutbox = view.findViewById(R.id.btn_outbox);
        btnInbox = view.findViewById(R.id.btn_inbox);
        btnStaffView = view.findViewById(R.id.btn_staff_view);
        cardHealth = view.findViewById(R.id.card_health);
        cardFacilities = view.findViewById(R.id.card_facilities);
        cardLibrary = view.findViewById(R.id.card_library);
    }

    private void setupClickListeners() {
        btnOutbox.setOnClickListener(v -> openOutbox());
        btnInbox.setOnClickListener(v -> openInbox());
        btnStaffView.setOnClickListener(v -> navigateToStaffIfAllowed());


        // Department card clicks
        cardHealth.setOnClickListener(v -> openWriteSuggestion("Health"));
        cardFacilities.setOnClickListener(v -> openWriteSuggestion("Facilities"));
        cardLibrary.setOnClickListener(v -> openWriteSuggestion("Library"));
    }


    private void openWriteSuggestion(String department) {
        WriteSuggestionFragment fragment = WriteSuggestionFragment.newInstance(department, null, false);
        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();

    }

    private void openOutbox() {
        SuggestionOutboxFragment fragment = new SuggestionOutboxFragment();
        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void openInbox() {
        SuggestionInboxFragment fragment = new SuggestionInboxFragment();
        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void navigateToStaffIfAllowed() {
        btnStaffView.setEnabled(false);
        userRoleService.checkCurrentUserRole(new UserRoleService.RoleCheckListener() {
            @Override
            public void onRoleChecked(boolean isStaff, String department, UserRoleService.UserRole userRole) {
                btnStaffView.setEnabled(true);
                if (isStaff) {
                    SuggestionStaffDashboardFragment staffFragment = new SuggestionStaffDashboardFragment();
                    getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, staffFragment)
                        .addToBackStack(null)
                        .commit();
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "You don't have staff access", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception error) {
                btnStaffView.setEnabled(true);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error checking access: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
