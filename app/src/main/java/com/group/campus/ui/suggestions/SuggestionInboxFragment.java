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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.group.campus.R;
import com.group.campus.models.Suggestion;
import com.group.campus.service.SuggestionsService;
import com.group.campus.service.UserRoleService;

import java.util.ArrayList;
import java.util.List;

/**
 * User's personal inbox - shows conversations they started
 * Available to all users (students and staff)
 * Shows replies to suggestions they submitted to any department
 */
public class SuggestionInboxFragment extends Fragment {

    private RecyclerView rvConversations;
    private LinearLayout emptyState;
    private FloatingActionButton fabNewConversation;
    private ImageView btnBack;
    private TextView tvHeaderTitle, tvStaffInfo;

    private SuggestionConversationAdapter adapter;
    private final List<com.group.campus.model.SuggestionConversation> conversations = new ArrayList<>();
    private SuggestionsService suggestionsService;
    private UserRoleService userRoleService;
    private ListenerRegistration conversationListener;
    private boolean isStaffUser = false;
    private String userDepartment = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggestion_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeServices();
        initViews(view);
        setupRecyclerView();
        setupNavigation();
        checkUserRoleAndLoadConversations();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (conversationListener != null) {
            conversationListener.remove();
        }
    }

    private void initializeServices() {
        suggestionsService = new SuggestionsService();
        userRoleService = new UserRoleService();
    }

    private void initViews(View view) {
        rvConversations = view.findViewById(R.id.rv_conversations);
        emptyState = view.findViewById(R.id.empty_state);
        fabNewConversation = view.findViewById(R.id.fab_new_conversation);
        btnBack = view.findViewById(R.id.btn_back);
        tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        tvStaffInfo = view.findViewById(R.id.tv_staff_info);

        // Set header title
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText("My Suggestion Inbox");
        }
    }

    private void setupRecyclerView() {
        adapter = new SuggestionConversationAdapter(conversations, this::onConversationClick);
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConversations.setAdapter(adapter);
    }

    private void setupNavigation() {
        btnBack.setOnClickListener(v -> navigateBack());

        if (fabNewConversation != null) {
            fabNewConversation.setOnClickListener(v -> navigateToWriteSuggestion());
        }
    }

    /**
     * Check user role and load their personal conversations
     */
    private void checkUserRoleAndLoadConversations() {
        userRoleService.checkCurrentUserRole(new UserRoleService.RoleCheckListener() {
            @Override
            public void onRoleChecked(boolean isStaff, String department, UserRoleService.UserRole userRole) {
                isStaffUser = isStaff;
                userDepartment = department;

                // Update UI to show staff info if applicable
                updateStaffInfo(userRole, isStaff, department);

                // Load user's personal conversations (suggestions they submitted)
                loadUserConversations();
            }

            @Override
            public void onError(Exception error) {
                // Default to student behavior if error checking role
                loadUserConversations();
            }
        });
    }

    /**
     * Update UI to show staff information if user is staff
     */
    private void updateStaffInfo(UserRoleService.UserRole userRole, boolean isStaff, String department) {
        if (tvStaffInfo != null && isStaff && department != null) {
            String displayName = (userRole != null && userRole.name != null && !userRole.name.isEmpty())
                ? userRole.name
                : "Staff Member";
            tvStaffInfo.setText(displayName + " - " + department + " Department");
            tvStaffInfo.setVisibility(View.VISIBLE);
        } else if (tvStaffInfo != null) {
            tvStaffInfo.setVisibility(View.GONE);
        }
    }

    /**
     * Load conversations that the current user started (their submitted suggestions)
     * INBOX VERSION: Only show conversations where staff has replied
     */
    private void loadUserConversations() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            updateEmptyState();
            return;
        }

        String userId = currentUser.getUid();

        // Use the new inbox method to show only conversations with staff replies
        conversationListener = suggestionsService.listenToUserInbox(userId, new SuggestionsService.ConversationListener() {
            @Override
            public void onConversationsChanged(List<Suggestion> newSuggestions) {
                updateConversationsList(newSuggestions);
            }

            @Override
            public void onError(Exception error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading inbox: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                updateEmptyState();
            }
        });
    }

    /**
     * Update conversations list and notify adapter
     */
    private void updateConversationsList(List<Suggestion> newSuggestions) {
        int previousSize = conversations.size();
        conversations.clear();

        // Convert Suggestion objects to SuggestionConversation objects with error handling
        for (Suggestion suggestion : newSuggestions) {
            try {
                // Create SuggestionConversation with proper mapping using correct method names
                com.group.campus.model.SuggestionConversation conversation = new com.group.campus.model.SuggestionConversation();
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

    /**
     * Handle conversation click - navigate to conversation detail
     */
    private void onConversationClick(com.group.campus.model.SuggestionConversation conversation) {
        // Navigate to conversation detail where user can see replies
        Bundle args = new Bundle();
        args.putString("conversationId", conversation.getId());
        args.putBoolean("replyAsStaff", false);

        SuggestionsFragment chat = new SuggestionsFragment();
        chat.setArguments(args);

        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, chat)
            .addToBackStack(null)
            .commit();
    }

    /**
     * Navigate to write new suggestion
     */
    private void navigateToWriteSuggestion() {
        // Navigate to your existing suggestions fragment or write suggestion screen
        // You can pass staff info if needed
        Bundle args = new Bundle();
        // New suggestions are written as user, not staff
        args.putBoolean("replyAsStaff", false);

        SuggestionsFragment chat = new SuggestionsFragment();
        chat.setArguments(args);

        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, chat)
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
