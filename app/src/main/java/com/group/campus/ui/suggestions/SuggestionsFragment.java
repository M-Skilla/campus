package com.group.campus.ui.suggestions;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.group.campus.HomeActivity;
import com.group.campus.R;
import com.group.campus.model.Suggestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SuggestionsFragment extends Fragment implements SuggestionAdapter.OnSuggestionClickListener {

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvSuggestions;
    private EditText etMessage;
    private ImageView btnSend;
    private ImageView btnClip;
    private ImageView btnDepartment;
    private LinearLayout attachmentPreview;
    private ImageView ivAttachmentPreview;
    private TextView tvAttachmentName;
    private ImageView btnRemoveAttachment;

    // Data & Adapter
    private SuggestionAdapter adapter;
    private List<Suggestion> suggestions;
    private Uri selectedAttachmentUri;
    private String selectedAttachmentName;
    private String selectedAttachmentType; // MIME type like image/*, video/*, application/pdf

    private BottomNavigationView bottomNav;

    private final List<String> departments = Arrays.asList(
        "Computer Science",
        "Engineering",
        "Business",
        "Library",
        "Facilities"
    );
    private String selectedDepartment = null; // require explicit selection before send

    // Activity Result launcher for file picking
    private ActivityResultLauncher<Intent> pickFileLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_suggestions, container, false);
        } catch (Throwable inflateError) {
            // Programmatic fallback to avoid Premature end of file crashes
            Context ctx = requireContext();
            ConstraintLayout root = new ConstraintLayout(ctx);
            root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            RecyclerView rv = new RecyclerView(ctx);
            rv.setId(R.id.rvSuggestions);
            rv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            root.addView(rv);

            LinearLayout footer = new LinearLayout(ctx);
            footer.setOrientation(LinearLayout.HORIZONTAL);
            footer.setId(R.id.inputBottomContainer);
            ConstraintLayout.LayoutParams flp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            flp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            footer.setLayoutParams(flp);

            ImageView clip = new ImageView(ctx);
            clip.setId(R.id.btnClip);
            clip.setImageResource(R.drawable.ic_attach_file);
            clip.setColorFilter(0xFFBFC2C6);
            LayoutParams clipLp = new LayoutParams(dp(44), dp(44));
            clip.setLayoutParams(clipLp);
            footer.addView(clip);

            EditText input = new EditText(ctx);
            input.setId(R.id.etMessage);
            input.setHint(R.string.type_suggestion_hint);
            LayoutParams inLp = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            input.setLayoutParams(inLp);
            footer.addView(input);

            ImageView send = new ImageView(ctx);
            send.setId(R.id.btnSend);
            send.setImageResource(R.drawable.ic_send);
            LayoutParams sendLp = new LayoutParams(dp(48), dp(48));
            send.setLayoutParams(sendLp);
            footer.addView(send);

            root.addView(footer);

            return root;
        }
    }

    private int dp(int dps) { return Math.round(dps * requireContext().getResources().getDisplayMetrics().density); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar();
        setupDepartmentSelector();
        setupRecyclerView();
        setupActivityResultLaunchers();
        setupInputHandling();
        hideBottomNavigation();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        rvSuggestions = view.findViewById(R.id.rvSuggestions);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnClip = view.findViewById(R.id.btnClip);
        btnDepartment = view.findViewById(R.id.btnDepartment);
        attachmentPreview = view.findViewById(R.id.attachmentPreview);
        ivAttachmentPreview = view.findViewById(R.id.ivAttachmentPreview);
        tvAttachmentName = view.findViewById(R.id.tvAttachmentName);
        btnRemoveAttachment = view.findViewById(R.id.btnRemoveAttachment);

        if (getActivity() instanceof HomeActivity) {
            bottomNav = getActivity().findViewById(R.id.bottomNav);
        }
    }

    private void setupToolbar() {
        if (toolbar == null) return;
        toolbar.setNavigationOnClickListener(v -> {
            showBottomNavigation();
            if (getActivity() instanceof HomeActivity && bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.announcementsItem);
            }
        });
    }

    private void setupDepartmentSelector() {
        if (btnDepartment == null) return;
        btnDepartment.setOnClickListener(v -> showDepartmentChooser());
        btnDepartment.setOnLongClickListener(v -> {
            Toast.makeText(requireContext(), selectedDepartment == null ? getString(R.string.choose_department) : selectedDepartment, Toast.LENGTH_SHORT).show();
            return true;
        });
        updateSendButtonState();
    }

    private void showDepartmentChooser() {
        final String[] items = departments.toArray(new String[0]);
        int checked = selectedDepartment == null ? -1 : departments.indexOf(selectedDepartment);
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.choose_department)
            .setSingleChoiceItems(items, checked, (dialog, which) -> {
                selectedDepartment = items[which];
                dialog.dismiss();
                updateSendButtonState();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void setupRecyclerView() {
        suggestions = new ArrayList<>();
        adapter = new SuggestionAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvSuggestions.setLayoutManager(layoutManager);
        rvSuggestions.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                rvSuggestions.smoothScrollToPosition(Math.max(0, adapter.getItemCount() - 1));
            }
        });
    }

    private void setupActivityResultLaunchers() {
        pickFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onPickFileResult);
    }

    private void onPickFileResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            selectedAttachmentUri = result.getData().getData();
            if (selectedAttachmentUri != null) {
                // Persist permission if granted by provider
                try {
                    final int takeFlags = result.getData().getFlags() &
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    requireContext().getContentResolver().takePersistableUriPermission(selectedAttachmentUri, takeFlags);
                } catch (Exception ignored) {}

                selectedAttachmentType = resolveMimeType(selectedAttachmentUri);
                selectedAttachmentName = resolveDisplayName(selectedAttachmentUri);
                showAttachmentPreview();
                updateSendButtonState();
            }
        }
    }

    private String resolveMimeType(Uri uri) {
        ContentResolver cr = requireContext().getContentResolver();
        String type = cr.getType(uri);
        if (type != null) return type; // e.g., image/jpeg, video/mp4, application/pdf
        // Fallback by extension
        String path = uri.toString().toLowerCase();
        if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") || path.endsWith(".webp")) return "image/*";
        if (path.endsWith(".mp4") || path.endsWith(".mkv") || path.endsWith(".mov")) return "video/*";
        if (path.endsWith(".pdf")) return "application/pdf";
        if (path.endsWith(".doc") || path.endsWith(".docx")) return "application/msword";
        return "application/octet-stream";
    }

    private String resolveDisplayName(Uri uri) {
        String name = null;
        try (android.database.Cursor c = requireContext().getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = c.getString(idx);
            }
        } catch (Exception ignored) {}
        if (name != null) return name;
        String path = uri.getPath();
        if (path != null && path.contains("/")) return path.substring(path.lastIndexOf('/') + 1);
        return getString(R.string.attachment_icon);
    }

    private void setupInputHandling() {
        if (btnClip != null) btnClip.setOnClickListener(v -> openFilePicker());
        if (btnSend != null) btnSend.setOnClickListener(v -> sendSuggestion());
        if (btnRemoveAttachment != null) btnRemoveAttachment.setOnClickListener(v -> removeAttachment());
        if (etMessage != null) {
            etMessage.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND) { sendSuggestion(); return true; }
                return false;
            });
            etMessage.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateSendButtonState(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
        updateSendButtonState();
        if (etMessage != null) {
            etMessage.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && rvSuggestions != null && adapter != null) {
                    rvSuggestions.post(() -> rvSuggestions.scrollToPosition(Math.max(0, adapter.getItemCount() - 1)));
                }
            });
        }
    }

    private void updateSendButtonState() {
        boolean hasText = etMessage != null && !etMessage.getText().toString().trim().isEmpty();
        boolean hasAttachment = selectedAttachmentUri != null;
        boolean deptSelected = selectedDepartment != null && !selectedDepartment.trim().isEmpty();
        boolean canSend = (hasText || hasAttachment) && deptSelected;
        if (btnSend != null) {
            btnSend.setEnabled(canSend);
            btnSend.setAlpha(canSend ? 1.0f : 0.5f);
        }
    }

    private void sendSuggestion() {
        if (selectedDepartment == null || selectedDepartment.trim().isEmpty()) {
            new AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_department)
                .setMessage(R.string.select_recipient_hint)
                .setPositiveButton(android.R.string.ok, null)
                .show();
            return;
        }
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty() && selectedAttachmentUri == null) return;
        Suggestion suggestion = new Suggestion(messageText, true); // no placeholder title
        suggestion.setId("temp_" + System.currentTimeMillis());
        suggestion.setAuthorName("You");
        if (selectedAttachmentUri != null) {
            suggestion.setAttachmentUrl(selectedAttachmentUri.toString());
            suggestion.setAttachmentType(selectedAttachmentType != null ? selectedAttachmentType : "application/octet-stream");
        }
        List<Suggestion> newList = new ArrayList<>(suggestions);
        newList.add(suggestion);
        suggestions = newList;
        adapter.submitList(new ArrayList<>(suggestions));
        etMessage.setText("");
        removeAttachment();
        etMessage.requestFocus();
        rvSuggestions.post(() -> rvSuggestions.scrollToPosition(Math.max(0, adapter.getItemCount() - 1)));
        simulateSending(suggestion);
        Snackbar.make(requireView(), R.string.send_suggestion, Snackbar.LENGTH_SHORT).show();
    }

    private void simulateSending(Suggestion suggestion) {
        suggestion.setStatus(Suggestion.SuggestionStatus.SENDING);
        adapter.notifyItemChanged(suggestions.size() - 1);
        etMessage.postDelayed(() -> {
            suggestion.setStatus(Suggestion.SuggestionStatus.SENT);
            adapter.notifyItemChanged(suggestions.size() - 1);
            etMessage.postDelayed(() -> {
                suggestion.setStatus(Suggestion.SuggestionStatus.DELIVERED);
                adapter.notifyItemChanged(suggestions.size() - 1);
            }, 800);
        }, 800);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "video/*", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickFileLauncher.launch(intent);
    }

    private void showAttachmentPreview() {
        if (attachmentPreview == null || tvAttachmentName == null || ivAttachmentPreview == null) return;
        if (selectedAttachmentUri != null) {
            attachmentPreview.setVisibility(View.VISIBLE);
            tvAttachmentName.setText(selectedAttachmentName);
            // Load visual preview
            if (selectedAttachmentType != null && selectedAttachmentType.startsWith("image")) {
                Glide.with(this).load(selectedAttachmentUri).into(ivAttachmentPreview);
            } else if (selectedAttachmentType != null && selectedAttachmentType.startsWith("video")) {
                Glide.with(this).load(selectedAttachmentUri).frame(1_000_000).into(ivAttachmentPreview);
            } else if (selectedAttachmentType != null && selectedAttachmentType.contains("pdf")) {
                ivAttachmentPreview.setImageResource(R.drawable.ic_picture_as_pdf);
            } else if (selectedAttachmentType != null && (selectedAttachmentType.contains("word") || selectedAttachmentType.contains("msword") || selectedAttachmentType.contains("document"))) {
                ivAttachmentPreview.setImageResource(R.drawable.ic_document);
            } else {
                ivAttachmentPreview.setImageResource(R.drawable.ic_attachment);
            }
        }
    }

    private void removeAttachment() {
        selectedAttachmentUri = null;
        selectedAttachmentName = null;
        selectedAttachmentType = null;
        if (attachmentPreview != null) attachmentPreview.setVisibility(View.GONE);
        updateSendButtonState();
    }

    private void hideBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.clearAnimation();
            bottomNav.setVisibility(View.GONE); // reclaim layout height
        }
    }

    private void showBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.clearAnimation();
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSuggestionLongClick(Suggestion suggestion) {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.message_status)
            .setItems(new String[]{getString(R.string.send_message), getString(android.R.string.cancel)}, (dialog, which) -> {})
            .show();
    }

    @Override
    public void onAttachmentClick(Suggestion suggestion) {
        String type = suggestion.getAttachmentType();
        String url = suggestion.getAttachmentUrl();
        if (type != null && url != null && (type.startsWith("image") || type.startsWith("video"))) {
            AttachmentViewerDialogFragment.newInstance(url, type)
                .show(getChildFragmentManager(), "attachment_viewer");
            return;
        }
        try {
            Uri uri = Uri.parse(url);
            Intent view = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, type != null ? type : "application/octet-stream")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (view.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(view);
            } else {
                Toast.makeText(getContext(), R.string.attachment_preview, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.attachment_preview, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showBottomNavigation();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideBottomNavigation();
    }
}
