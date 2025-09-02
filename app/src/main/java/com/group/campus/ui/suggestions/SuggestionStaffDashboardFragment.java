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

import com.google.firebase.firestore.ListenerRegistration;
import com.group.campus.R;
import com.group.campus.models.Suggestion;
import com.group.campus.model.SuggestionConversation;
import com.group.campus.service.SuggestionsService;
import com.group.campus.service.UserRoleService;

import java.util.ArrayList;
import java.util.List;

public class SuggestionStaffDashboardFragment extends Fragment {

    private RecyclerView rvConversations;
    private LinearLayout emptyState, loadingState, accessDeniedState;
    private ImageView btnBack;
    private TextView tvStaffName, tvStaffDepartment, tvAccessDeniedMessage;

    private SuggestionConversationAdapter adapter;
    private final List<SuggestionConversation> conversations = new ArrayList<>();
    private SuggestionsService suggestionsService;
    private UserRoleService userRoleService;
    private ListenerRegistration conversationListener;
    private String staffDepartment = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggestion_staff_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        suggestionsService = new SuggestionsService();
        userRoleService = new UserRoleService();
        findViews(view);
        setupRecyclerView();
        btnBack.setOnClickListener(v -> navigateBack());
        checkUserAccess();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (conversationListener != null) {
            conversationListener.remove();
            conversationListener = null;
        }
    }

    private void findViews(View view) {
        rvConversations = view.findViewById(R.id.rv_conversations);
        emptyState = view.findViewById(R.id.empty_state);
        loadingState = view.findViewById(R.id.loading_state);
        accessDeniedState = view.findViewById(R.id.access_denied_state);
        btnBack = view.findViewById(R.id.btn_back);
        tvStaffName = view.findViewById(R.id.tv_staff_name);
        tvStaffDepartment = view.findViewById(R.id.tv_staff_department);
        tvAccessDeniedMessage = view.findViewById(R.id.tv_access_denied_message);
    }

    private void setupRecyclerView() {
        adapter = new SuggestionConversationAdapter(conversations, this::handleConversationClick);
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConversations.setAdapter(adapter);
    }

    private void checkUserAccess() {
        showState(StateType.LOADING);
        userRoleService.checkCurrentUserRole(new UserRoleService.RoleCheckListener() {
            @Override
            public void onRoleChecked(boolean isStaff, String department, UserRoleService.UserRole userRole) {
                if (getContext() == null) return;
                if (isStaff && department != null) {
                    staffDepartment = department;
                    updateHeader(userRole, department);
                    startListening(staffDepartment);
                    showState(StateType.CONTENT);
                } else {
                    showAccessDenied("You don't have staff access");
                }
            }

            @Override
            public void onError(Exception error) {
                showAccessDenied("Error verifying staff access: " + error.getMessage());
            }
        });
    }

    private void updateHeader(UserRoleService.UserRole userRole, String department) {
        if (tvStaffName != null) {
            String displayName = (userRole != null && userRole.name != null && !userRole.name.isEmpty())
                ? userRole.name
                : (userRole != null && userRole.registrationNumber != null ? userRole.registrationNumber : "Staff");
            tvStaffName.setText("Welcome, " + displayName);
        }
        if (tvStaffDepartment != null) {
            tvStaffDepartment.setVisibility(View.VISIBLE);
            tvStaffDepartment.setText(department + " Department");
        }
    }

    private void startListening(String department) {
        if (conversationListener != null) {
            conversationListener.remove();
            conversationListener = null;
        }
        conversationListener = suggestionsService.listenToStaffConversations(department, new SuggestionsService.ConversationListener() {
            @Override
            public void onConversationsChanged(List<Suggestion> newSuggestions) {
                conversations.clear();
                for (Suggestion suggestion : newSuggestions) {
                    SuggestionConversation conversation = new SuggestionConversation();
                    conversation.setId(suggestion.getSuggestionId());
                    conversation.setStudentName(suggestion.getSenderName() != null ? suggestion.getSenderName() : "Unknown User");
                    conversation.setDepartment(suggestion.getReceiverDepartment() != null ? suggestion.getReceiverDepartment() : department);
                    conversation.setSubject(suggestion.getSubject() != null ? suggestion.getSubject() : "No Subject");
                    conversation.setMessage(suggestion.getText() != null ? suggestion.getText() : "");
                    conversation.setTimestamp(suggestion.getTimestamp());
                    conversation.setStatus(suggestion.getStatus() != null ? suggestion.getStatus() : "open");
                    conversation.setStudentId(suggestion.getSenderId() != null ? suggestion.getSenderId() : "unknown");
                    conversations.add(conversation);
                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(Exception error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading conversations: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                updateEmptyState();
            }
        });
    }

    private void handleConversationClick(SuggestionConversation conversation) {
        WriteSuggestionFragment fragment = WriteSuggestionFragment.newInstance(conversation.getDepartment(), conversation.getId(), true);
        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void updateEmptyState() {
        if (conversations.isEmpty()) {
            showState(StateType.EMPTY);
        } else {
            showState(StateType.CONTENT);
        }
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }

    private enum StateType { LOADING, CONTENT, EMPTY, ACCESS_DENIED }

    private void showState(StateType state) {
        if (loadingState != null) loadingState.setVisibility(View.GONE);
        if (emptyState != null) emptyState.setVisibility(View.GONE);
        if (accessDeniedState != null) accessDeniedState.setVisibility(View.GONE);
        if (rvConversations != null) rvConversations.setVisibility(View.GONE);

        switch (state) {
            case LOADING:
                if (loadingState != null) loadingState.setVisibility(View.VISIBLE);
                break;
            case CONTENT:
                if (rvConversations != null) rvConversations.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                break;
            case ACCESS_DENIED:
                if (accessDeniedState != null) accessDeniedState.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showAccessDenied(String message) {
        if (tvAccessDeniedMessage != null) tvAccessDeniedMessage.setText(message);
        showState(StateType.ACCESS_DENIED);
    }
}
