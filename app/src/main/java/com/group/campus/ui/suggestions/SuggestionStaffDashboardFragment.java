package com.group.campus.ui.suggestions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.ListenerRegistration;
import com.group.campus.R;
import com.group.campus.models.Suggestion;
import com.group.campus.model.SuggestionConversation;
import com.group.campus.service.SuggestionsService;
import com.group.campus.service.UserRoleService;
import com.group.campus.HomeActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Staff Dashboard Fragment - Automatically detects staff login and shows department suggestions
 * No manual setup required - works automatically when staff user logs in
 */
public class SuggestionStaffDashboardFragment extends Fragment {

    // UI Components
    private RecyclerView rvConversations;
    private LinearLayout emptyState, loadingState, accessDeniedState;
    private ImageView btnBack;
    private ChipGroup chipGroupDepartments;
    private TextView tvStaffName, tvStaffDepartment, tvAccessDeniedMessage;

    // Data and Services
    private SuggestionConversationAdapter adapter;
    private final List<SuggestionConversation> conversations = new ArrayList<>();
    private SuggestionsService suggestionsService;
    private UserRoleService userRoleService;
    private ListenerRegistration conversationListener;
    private String selectedDepartment = "";
    private String staffDepartment = "";
    private boolean isStaffUser = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggestion_staff_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide bottom navigation when this fragment is shown
        hideBottomNavigation();

        initializeServices();
        setupViews(view);
        checkUserAccess(); // Automatically verify if user is staff
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show bottom navigation when this fragment is destroyed
        showBottomNavigation();

        if (conversationListener != null) {
            conversationListener.remove();
        }
    }

    private void initializeServices() {
        suggestionsService = new SuggestionsService();
        userRoleService = new UserRoleService();
    }

    private void setupViews(View view) {
        findViews(view);
        setupRecyclerView();
        setupNavigation();
    }

    private void findViews(View view) {
        rvConversations = view.findViewById(R.id.rv_conversations);
        emptyState = view.findViewById(R.id.empty_state);
        loadingState = view.findViewById(R.id.loading_state);
        accessDeniedState = view.findViewById(R.id.access_denied_state);
        btnBack = view.findViewById(R.id.btn_back);
        chipGroupDepartments = view.findViewById(R.id.chip_group_departments);
        tvStaffName = view.findViewById(R.id.tv_staff_name);
        tvStaffDepartment = view.findViewById(R.id.tv_staff_department);
        tvAccessDeniedMessage = view.findViewById(R.id.tv_access_denied_message);
    }

    private void setupRecyclerView() {
        adapter = new SuggestionConversationAdapter(conversations, this::handleConversationClick);
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConversations.setAdapter(adapter);
    }

    private void setupNavigation() {
        btnBack.setOnClickListener(view -> navigateBack());
    }

    /**
     * Automatically check if logged-in user has staff access
     */
    private void checkUserAccess() {
        showState(StateType.LOADING);

        userRoleService.checkCurrentUserRole(new UserRoleService.RoleCheckListener() {
            @Override
            public void onRoleChecked(boolean isStaff, String department, UserRoleService.UserRole userRole) {
                if (getContext() == null) return;

                isStaffUser = isStaff;
                staffDepartment = department;

                if (isStaff && department != null) {
                    // User is verified staff - setup dashboard for their department
                    setupStaffDashboard(userRole, department);
                } else {
                    // User is not staff or not active - show access denied
                    showAccessDenied();
                }
            }

            @Override
            public void onError(Exception error) {
                if (getContext() != null) {
                    showAccessDenied("Error verifying staff access: " + error.getMessage());
                }
            }
        });
    }

    /**
     * Setup dashboard for verified staff member
     */
    private void setupStaffDashboard(UserRoleService.UserRole userRole, String department) {
        // Update header with staff info
        updateStaffHeader(userRole, department);

        // Setup department filter (defaulting to staff's department)
        setupDepartmentFilter(department);

        // Load conversations for staff's department
        selectedDepartment = department;
        loadConversationsForDepartment();

        showState(StateType.CONTENT);
    }

    /**
     * Update header with staff member information
     */
    private void updateStaffHeader(UserRoleService.UserRole userRole, String department) {
        if (tvStaffName != null) {
            String displayName = (userRole != null && userRole.name != null && !userRole.name.isEmpty())
                ? userRole.name
                : (userRole != null && userRole.registrationNumber != null ? userRole.registrationNumber : "Staff");
            tvStaffName.setText("Welcome, " + displayName);
        }

        if (tvStaffDepartment != null) {
            tvStaffDepartment.setText(department + " Department");
        }
    }

    /**
     * Setup department filter chips with staff's department pre-selected
     */
    private void setupDepartmentFilter(String staffDepartment) {
        chipGroupDepartments.setSingleSelection(true);
        chipGroupDepartments.setOnCheckedStateChangeListener(this::handleDepartmentSelection);

        // Pre-select staff's department
        selectDepartmentChip(staffDepartment);
    }

    /**
     * Select the appropriate department chip
     */
    private void selectDepartmentChip(String department) {
        switch (department) {
            case "Health":
                chipGroupDepartments.check(R.id.chip_health_staff);
                break;
            case "Facilities":
                chipGroupDepartments.check(R.id.chip_facilities_staff);
                break;
            case "Library":
                chipGroupDepartments.check(R.id.chip_library_staff);
                break;
        }
    }

    private void handleDepartmentSelection(ChipGroup group, List<Integer> checkedIds) {
        if (!checkedIds.isEmpty()) {
            Chip selectedChip = chipGroupDepartments.findViewById(checkedIds.get(0));
            String newDepartment = mapChipToDepartment(selectedChip.getText().toString());

            if (!newDepartment.equals(selectedDepartment)) {
                selectedDepartment = newDepartment;
                loadConversationsForDepartment();
            }
        }
    }

    private String mapChipToDepartment(String chipText) {
        switch (chipText) {
            case "Health": return "Health";
            case "Facilities": return "Facilities";
            case "Library": return "Library";
            default: return staffDepartment; // Fallback to staff's department
        }
    }

    private void loadConversationsForDepartment() {
        // Clean up existing listener
        if (conversationListener != null) {
            conversationListener.remove();
            conversationListener = null;
        }

        // Setup new listener for selected department
        conversationListener = suggestionsService.listenToStaffConversations(
            selectedDepartment,
            new ConversationUpdateListener()
        );
    }

    private void handleConversationClick(SuggestionConversation conversation) {
        // Navigate to conversation detail
        navigateToConversationDetail(conversation);
    }

    private void navigateToConversationDetail(SuggestionConversation conversation) {
        Bundle args = new Bundle();
        args.putString("conversationId", conversation.getId());
        args.putString("studentName", conversation.getStudentName());
        args.putString("department", conversation.getDepartment());
        args.putBoolean("replyAsStaff", true);

        SuggestionsFragment chat = new SuggestionsFragment();
        chat.setArguments(args);

        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, chat)
            .addToBackStack(null)
            .commit();
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }

    private void showAccessDenied() {
        showAccessDenied("You don't have staff access to the suggestion system.\n\nPlease contact your administrator to grant you staff permissions.");
    }

    private void showAccessDenied(String message) {
        if (tvAccessDeniedMessage != null) {
            tvAccessDeniedMessage.setText(message);
        }
        showState(StateType.ACCESS_DENIED);
    }

    private void updateEmptyState() {
        if (conversations.isEmpty()) {
            showState(StateType.EMPTY);
        } else {
            showState(StateType.CONTENT);
        }
    }

    private enum StateType {
        LOADING, CONTENT, EMPTY, ACCESS_DENIED
    }

    private void showState(StateType state) {
        // Hide all states first
        if (loadingState != null) loadingState.setVisibility(View.GONE);
        if (emptyState != null) emptyState.setVisibility(View.GONE);
        if (accessDeniedState != null) accessDeniedState.setVisibility(View.GONE);
        if (rvConversations != null) rvConversations.setVisibility(View.GONE);
        if (chipGroupDepartments != null) chipGroupDepartments.setVisibility(View.GONE);

        // Show appropriate state
        switch (state) {
            case LOADING:
                if (loadingState != null) loadingState.setVisibility(View.VISIBLE);
                break;
            case CONTENT:
                if (rvConversations != null) rvConversations.setVisibility(View.VISIBLE);
                if (chipGroupDepartments != null) chipGroupDepartments.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                if (chipGroupDepartments != null) chipGroupDepartments.setVisibility(View.VISIBLE);
                break;
            case ACCESS_DENIED:
                if (accessDeniedState != null) accessDeniedState.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Hide the bottom navigation when this fragment is active
     */
    private void hideBottomNavigation() {
        if (getActivity() instanceof HomeActivity) {
            HomeActivity homeActivity = (HomeActivity) getActivity();
            if (homeActivity.getCustomBottomNavView() != null) {
                homeActivity.getCustomBottomNavView().setVisibility(View.GONE);
            }
        }
    }

    /**
     * Show the bottom navigation when leaving this fragment
     */
    private void showBottomNavigation() {
        if (getActivity() instanceof HomeActivity) {
            HomeActivity homeActivity = (HomeActivity) getActivity();
            if (homeActivity.getCustomBottomNavView() != null) {
                homeActivity.getCustomBottomNavView().setVisibility(View.VISIBLE);
            }
        }
    }

    private void cleanupResources() {
        if (conversationListener != null) {
            conversationListener.remove();
            conversationListener = null;
        }
    }

    /**
     * Inner class to handle conversation updates from Firestore
     */
    private class ConversationUpdateListener implements SuggestionsService.ConversationListener {

        @Override
        public void onConversationsChanged(List<Suggestion> newSuggestions) {
            updateConversationsList(newSuggestions);
        }

        @Override
        public void onError(Exception error) {
            handleError("Error loading conversations", error);
        }

        private void updateConversationsList(List<Suggestion> newSuggestions) {
            int previousSize = conversations.size();
            conversations.clear();

            // Convert Suggestions to SuggestionConversations properly
            for (Suggestion suggestion : newSuggestions) {
                try {
                    SuggestionConversation conversation = createConversationFromSuggestion(suggestion);
                    conversations.add(conversation);
                } catch (Exception e) {
                    // Skip invalid suggestions
                    continue;
                }
            }

            // Efficient RecyclerView updates
            if (adapter != null) {
                if (previousSize > 0) {
                    adapter.notifyItemRangeRemoved(0, previousSize);
                }
                if (!conversations.isEmpty()) {
                    adapter.notifyItemRangeInserted(0, conversations.size());
                }
            }

            updateEmptyState();
        }

        private SuggestionConversation createConversationFromSuggestion(Suggestion suggestion) {
            // Create a proper SuggestionConversation from Suggestion using correct method names
            SuggestionConversation conversation = new SuggestionConversation();
            conversation.setId(suggestion.getSuggestionId());
            conversation.setStudentName(suggestion.getSenderName() != null ? suggestion.getSenderName() : "Unknown User");
            conversation.setDepartment(suggestion.getReceiverDepartment() != null ? suggestion.getReceiverDepartment() : "General");
            conversation.setSubject(suggestion.getSubject() != null ? suggestion.getSubject() : "No Subject");
            conversation.setMessage(suggestion.getText() != null ? suggestion.getText() : "");
            conversation.setTimestamp(suggestion.getTimestamp());
            conversation.setStatus(suggestion.getStatus() != null ? suggestion.getStatus() : "open");
            conversation.setStudentId(suggestion.getSenderId() != null ? suggestion.getSenderId() : "unknown");
            return conversation;
        }

        private void handleError(String message, Exception error) {
            if (getContext() != null) {
                String errorMessage = message + ": " + (error.getMessage() != null ? error.getMessage() : "Unknown error");
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                showState(StateType.EMPTY);
            }
        }
    }
}
