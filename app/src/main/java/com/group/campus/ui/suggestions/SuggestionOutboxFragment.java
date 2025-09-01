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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.group.campus.R;
import com.group.campus.models.Suggestion;
import com.group.campus.service.SuggestionsService;
import com.group.campus.service.UserRoleService;
import com.group.campus.model.SuggestionConversation;
import com.group.campus.HomeActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Outbox Fragment - Shows user's sent suggestions with delivery status
 */
public class SuggestionOutboxFragment extends Fragment {

    private RecyclerView rvConversations;
    private LinearLayout emptyState;
    private ImageView btnBack;
    private TextView tvStaffInfo;

    private SuggestionOutboxAdapter adapter;
    private final List<SuggestionConversation> conversations = new ArrayList<>();
    private SuggestionsService suggestionsService;
    private UserRoleService userRoleService;
    private ListenerRegistration conversationListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggestion_outbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide bottom navigation when this fragment is shown
        hideBottomNavigation();

        initializeServices();
        initViews(view);
        setupRecyclerView();
        setupNavigation();
        checkUserRoleAndLoadConversations();
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

    private void initializeServices() {
        suggestionsService = new SuggestionsService();
        userRoleService = new UserRoleService();
    }

    private void initViews(View view) {
        rvConversations = view.findViewById(R.id.rv_conversations);
        emptyState = view.findViewById(R.id.empty_state);
        btnBack = view.findViewById(R.id.btn_back);
        tvStaffInfo = view.findViewById(R.id.tv_staff_info);
    }

    private void setupRecyclerView() {
        adapter = new SuggestionOutboxAdapter(conversations, this::onConversationClick);
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConversations.setAdapter(adapter);
    }

    private void setupNavigation() {
        btnBack.setOnClickListener(v -> navigateBack());
    }

    private void checkUserRoleAndLoadConversations() {
        userRoleService.checkCurrentUserRole(new UserRoleService.RoleCheckListener() {
            @Override
            public void onRoleChecked(boolean isStaff, String department, UserRoleService.UserRole userRole) {
                updateStaffInfo(userRole, isStaff, department);
                loadUserConversations();
            }

            @Override
            public void onError(Exception error) {
                loadUserConversations();
            }
        });
    }

    private void updateStaffInfo(UserRoleService.UserRole userRole, boolean isStaff, String department) {
        if (tvStaffInfo != null && isStaff && department != null) {
            String displayName = (userRole != null && userRole.name != null && !userRole.name.isEmpty())
                ? userRole.name
                : "Staff Member";
            tvStaffInfo.setText(String.format("%s - %s Department", displayName, department));
            tvStaffInfo.setVisibility(View.VISIBLE);
        } else if (tvStaffInfo != null) {
            tvStaffInfo.setVisibility(View.GONE);
        }
    }

    private void loadUserConversations() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            updateEmptyState();
            return;
        }

        String userId = currentUser.getUid();

        // Use the new outbox method to show only sent messages
        conversationListener = suggestionsService.listenToUserOutbox(userId, new SuggestionsService.ConversationListener() {
            @Override
            public void onConversationsChanged(List<Suggestion> newSuggestions) {
                updateConversationsList(newSuggestions);
            }

            @Override
            public void onError(Exception error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading sent messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                updateEmptyState();
            }
        });
    }

    private void updateConversationsList(List<Suggestion> newSuggestions) {
        int previousSize = conversations.size();
        conversations.clear();

        // Convert Suggestion objects to SuggestionConversation objects with error handling
        for (Suggestion suggestion : newSuggestions) {
            try {
                SuggestionConversation conversation = new SuggestionConversation();
                conversation.setId(suggestion.getSuggestionId());
                conversation.setStudentId(suggestion.getSenderId() != null ? suggestion.getSenderId() : "unknown");
                conversation.setStudentName(suggestion.getSenderName() != null ? suggestion.getSenderName() : "Unknown User");
                conversation.setDepartment(suggestion.getReceiverDepartment() != null ? suggestion.getReceiverDepartment() : "General");
                conversation.setSubject(suggestion.getSubject() != null ? suggestion.getSubject() : "No Subject");
                conversation.setStatus(suggestion.getStatus() != null ? suggestion.getStatus() : "open");
                conversation.setLastMessageText(suggestion.getText() != null ? suggestion.getText() : "");

                // Handle timestamp conversion - getTimestamp() returns long
                conversation.setTimestamp(suggestion.getTimestamp());

                conversations.add(conversation);
            } catch (Exception e) {
                // Skip invalid suggestions and continue
                continue;
            }
        }

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

    private void onConversationClick(SuggestionConversation conversation) {
        // Open conversation in write mode (user can continue the conversation)
        WriteSuggestionFragment fragment = WriteSuggestionFragment.newInstance(
            conversation.getDepartment(),
            conversation.getId(),
            false
        );

        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void updateEmptyState() {
        if (conversations.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvConversations.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvConversations.setVisibility(View.VISIBLE);
        }
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
}
