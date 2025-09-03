package com.group.campus.ui.suggestions;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import com.group.campus.R;
import com.group.campus.model.SuggestionMessage;
import com.group.campus.models.Suggestion;
import com.group.campus.service.SuggestionsService;
import com.group.campus.service.UserRoleService;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WriteSuggestionFragment extends Fragment {

    // UI Components
    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageView btnSend, btnAttach, btnBack;
    private TextView tvDepartmentName, tvAttachmentName;
    private ImageView ivDepartmentIcon, ivEmptyIcon, btnRemoveAttachment;
    private LinearLayout attachmentPreview, emptyState;
    private View progressBar;

    // Data
    private SuggestionMessageAdapter adapter;
    private List<SuggestionMessage> messages = new ArrayList<>();
    private String department;
    private String conversationId;
    private Uri attachmentUri;
    private String attachmentName;
    private boolean replyAsStaff = false;
    private boolean isStaffUser = false;

    // Services
    private SuggestionsService suggestionsService;
    private UserRoleService userRoleService;
    private ListenerRegistration messageListener;

    // File picker
    private ActivityResultLauncher<Intent> filePickerLauncher;

    // Track last IME bottom inset
    private int lastImeBottom = 0;

    public static WriteSuggestionFragment newInstance(String department, String conversationId, boolean replyAsStaff) {
        WriteSuggestionFragment fragment = new WriteSuggestionFragment();
        Bundle args = new Bundle();
        args.putString("department", department);
        if (conversationId != null) {
            args.putString("conversationId", conversationId);
        }
        args.putBoolean("replyAsStaff", replyAsStaff);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            department = getArguments().getString("department");
            conversationId = getArguments().getString("conversationId");
            replyAsStaff = getArguments().getBoolean("replyAsStaff", false);
        }

        suggestionsService = new SuggestionsService();
        userRoleService = new UserRoleService();

        // Check user role
        userRoleService.checkCurrentUserRole(new UserRoleService.RoleCheckListener() {
            @Override
            public void onRoleChecked(boolean isStaff, String dept, UserRoleService.UserRole userRole) {
                isStaffUser = isStaff;
            }
            @Override
            public void onError(Exception error) { /* ignore */ }
        });

        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::handleFilePickerResult
        );

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBack();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_write_suggestion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupUI();
        applyBottomNavOffset();
        setupInputHandling();

        hideBottomNavigation();

        if (conversationId != null) {
            // Existing conversation - load messages
            loadConversationMessages();
            showChatView();
        } else {
            // New conversation - show empty state
            showEmptyState();
        }
    }


    private void applyBottomNavOffset() {
        if (getActivity() == null) return;
        View nav = getActivity().findViewById(R.id.customBottomNav);
        if (nav == null || getView() == null) return;
        View root = getView();
        View input = root.findViewById(R.id.input_container);
        View list = root.findViewById(R.id.rv_chat);
        View empty = root.findViewById(R.id.empty_state);

        ViewTreeObserver.OnGlobalLayoutListener navSizeListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() { updateBottomInsets(nav, input, list, empty, lastImeBottom); }
        };
        nav.getViewTreeObserver().addOnGlobalLayoutListener(navSizeListener);

        // Re-apply when input grows/shrinks (multiline)
        input.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom - top != oldBottom - oldTop) {
                updateBottomInsets(nav, input, list, empty, lastImeBottom);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            lastImeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            updateBottomInsets(nav, input, list, empty, lastImeBottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void updateBottomInsets(View nav, View input, View list, View empty, int imeBottom) {
        int navHeight = nav != null ? nav.getHeight() : 0;
        int bottomSpace = Math.max(navHeight, imeBottom);
        int inputHeight = input != null ? input.getHeight() : 0;
        if (input != null && input.getLayoutParams() instanceof ConstraintLayout.LayoutParams) {
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) input.getLayoutParams();
            if (lp.bottomMargin != bottomSpace) {
                lp.bottomMargin = bottomSpace;
                input.setLayoutParams(lp);
            }
        }
        if (list != null) {
            int padBottom = inputHeight + bottomSpace;
            if (list.getPaddingBottom() != padBottom) {
                list.setPadding(list.getPaddingLeft(), list.getPaddingTop(), list.getPaddingRight(), padBottom);
            }
            list.setClipToOutline(false);
        }
        if (empty != null) {
            int padBottom = inputHeight + bottomSpace;
            if (empty.getPaddingBottom() != padBottom) {
                empty.setPadding(empty.getPaddingLeft(), empty.getPaddingTop(), empty.getPaddingRight(), padBottom);
            }

        }
    }

    private void initViews(View view) {
        rvChat = view.findViewById(R.id.rv_chat);
        etMessage = view.findViewById(R.id.et_message);
        btnSend = view.findViewById(R.id.btn_send);
        btnAttach = view.findViewById(R.id.btn_attach);
        btnBack = view.findViewById(R.id.btn_back);
        tvDepartmentName = view.findViewById(R.id.tv_department_name);
        tvAttachmentName = view.findViewById(R.id.tv_attachment_name);
        ivDepartmentIcon = view.findViewById(R.id.iv_department_icon);
        ivEmptyIcon = view.findViewById(R.id.iv_empty_icon);
        btnRemoveAttachment = view.findViewById(R.id.btn_remove_attachment);
        attachmentPreview = view.findViewById(R.id.attachment_preview);
        emptyState = view.findViewById(R.id.empty_state);
        progressBar = view.findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> navigateBack());
        btnAttach.setOnClickListener(v -> openFilePicker());
        btnSend.setOnClickListener(v -> sendMessage());
        btnRemoveAttachment.setOnClickListener(v -> removeAttachment());
    }

    private void setupUI() {
        // Set department info label only (no hardcoded colors/icons per department)
        tvDepartmentName.setText(department + " Department");

        // Use a single consistent icon and theme colors
        int iconRes = R.drawable.ic_department;
        int bgColor = R.color.md_theme_primary;
        int iconColor = R.color.md_theme_onPrimary;

        ivDepartmentIcon.setImageResource(iconRes);
        ivDepartmentIcon.setBackgroundTintList(getResources().getColorStateList(bgColor, null));
        ivDepartmentIcon.setImageTintList(getResources().getColorStateList(iconColor, null));

        ivEmptyIcon.setImageResource(iconRes);
        ivEmptyIcon.setImageTintList(getResources().getColorStateList(iconColor, null));

        // Update empty state text
        TextView tvEmptyMessage = emptyState.findViewById(R.id.tv_empty_message);
        tvEmptyMessage.setText("Write your message to the " + department + " Department below");

        // Setup RecyclerView
        adapter = new SuggestionMessageAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);
    }

    private void setupInputHandling() {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateSendButtonState(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        updateSendButtonState();
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        rvChat.setVisibility(View.GONE);
    }

    private void showChatView() {
        emptyState.setVisibility(View.GONE);
        rvChat.setVisibility(View.VISIBLE);
    }

    private void loadConversationMessages() {
        if (conversationId == null) return;

        messageListener = suggestionsService.listenToMessages(conversationId, new SuggestionsService.MessageListener() {
            @Override
            public void onMessagesChanged(List<Suggestion.Reply> newReplies) {
                messages.clear();
                // Convert Suggestion.Reply to SuggestionMessage if needed, or update your adapter
                for (Suggestion.Reply reply : newReplies) {
                    // You'll need to create SuggestionMessage from Reply or update your message handling
                    // For now, I'll assume you have a conversion method
                    messages.add(convertReplyToMessage(reply));
                }
                adapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    rvChat.scrollToPosition(messages.size() - 1);
                    showChatView();
                }
            }

            @Override
            public void onError(Exception error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText) && attachmentUri == null) {
            Snackbar.make(requireView(), "Please enter a message or attach a file", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (conversationId == null) {
            createNewConversation(messageText);
        } else {
            addMessageToConversation(messageText);
        }
    }

    private void createNewConversation(String messageText) {
        progressBar.setVisibility(View.VISIBLE);
        btnSend.setEnabled(false);

        String subject = messageText.length() > 50 ? messageText.substring(0, 47) + "..." : messageText;

        suggestionsService.createConversation(department, subject, messageText, new SuggestionsService.OperationCallback() {
            @Override
            public void onSuccess(String newConversationId) {
                conversationId = newConversationId;
                clearInput();
                loadConversationMessages();
                showChatView();
                Snackbar.make(requireView(), "Suggestion sent successfully!", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception error) {
                progressBar.setVisibility(View.GONE);
                btnSend.setEnabled(true);
                updateSendButtonState();
                Toast.makeText(getContext(), "Failed to send suggestion: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addMessageToConversation(String messageText) {
        progressBar.setVisibility(View.VISIBLE);
        btnSend.setEnabled(false);

        if (attachmentUri != null) {
            suggestionsService.uploadAttachment(conversationId, attachmentUri, attachmentName, new SuggestionsService.OperationCallback() {
                @Override
                public void onSuccess(String attachmentUrl) {
                    sendMessageWithAttachment(messageText, attachmentUrl);
                }

                @Override
                public void onError(Exception error) {
                    sendMessageWithAttachment(messageText, null);
                }
            });
        } else {
            sendMessageWithAttachment(messageText, null);
        }
    }

    private void sendMessageWithAttachment(String messageText, String attachmentUrl) {
        boolean asStaff = replyAsStaff && isStaffUser;

        if (asStaff) {
            suggestionsService.addMessageAsStaff(conversationId, messageText, attachmentUrl, new SuggestionsService.OperationCallback() {
                @Override public void onSuccess(String result) { clearInput(); }
                @Override public void onError(Exception error) { onMessageSendFailed(error); }
            });
        } else {
            suggestionsService.addMessage(conversationId, messageText, attachmentUrl, new SuggestionsService.OperationCallback() {
                @Override public void onSuccess(String result) { clearInput(); }
                @Override public void onError(Exception error) { onMessageSendFailed(error); }
            });
        }
    }

    private void clearInput() {
        etMessage.setText("");
        attachmentUri = null;
        attachmentName = null;
        attachmentPreview.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        btnSend.setEnabled(true);
        updateSendButtonState();
    }

    private void onMessageSendFailed(Exception error) {
        progressBar.setVisibility(View.GONE);
        btnSend.setEnabled(true);
        updateSendButtonState();
        if (getContext() != null) {
            Toast.makeText(getContext(), "Failed to send message: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateSendButtonState() {
        boolean hasText = !TextUtils.isEmpty(etMessage.getText().toString().trim());
        boolean canSend = hasText || attachmentUri != null;

        btnSend.setEnabled(canSend);
        btnSend.setAlpha(canSend ? 1.0f : 0.5f);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "application/pdf", "application/msword",
                             "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(intent);
    }

    private void handleFilePickerResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Uri uri = result.getData().getData();
            if (uri != null) {
                attachmentUri = uri;
                displayAttachmentPreview(uri);
                updateSendButtonState();
            }
        }
    }

    private void displayAttachmentPreview(Uri uri) {
        try {
            ContentResolver contentResolver = requireContext().getContentResolver();
            String fileName = "Unknown file";
            try (android.database.Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex);
                }
            }
            attachmentName = fileName;
            tvAttachmentName.setText(fileName);
            attachmentPreview.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading attachment", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeAttachment() {
        attachmentUri = null;
        attachmentName = null;
        attachmentPreview.setVisibility(View.GONE);
        updateSendButtonState();
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
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

    private SuggestionMessage convertReplyToMessage(Suggestion.Reply reply) {
        SuggestionMessage m = new SuggestionMessage();
        m.setId(reply.getReplyId());
        m.setSenderId(reply.getSenderId());
        m.setSenderName(reply.getSenderName());
        m.setText(reply.getText());
        // Timestamp from long
        try {
            m.setTimestamp(new Timestamp(new Date(reply.getTimestamp())));
        } catch (Exception ignored) { /* leave default */ }
        String currentUid = FirebaseAuth.getInstance().getUid();
        boolean isMine = currentUid != null && currentUid.equals(reply.getSenderId());
        boolean fromStaff;
        if (isMine) {
            fromStaff = isStaffUser && replyAsStaff;
        } else {
            // Other party: if I'm student (replyAsStaff=false) then other is staff; if I'm staff then other is student
            fromStaff = !replyAsStaff;
        }
        m.setFromStaff(fromStaff);
        return m;
    }
}
