package com.group.campus.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.group.campus.R;
import com.group.campus.adapters.SelectedImagesAdapter;
import com.group.campus.models.Announcement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jp.wasabeef.richeditor.RichEditor;

public class AddAnnouncementDialog extends DialogFragment implements SelectedImagesAdapter.OnImageRemoveListener {

    private TextInputEditText etTitle, etDepartment;
    private AutoCompleteTextView actvVisibility;
    private RichEditor richEditor;
    private MaterialButton btnBold, btnItalic, btnUnderline, btnHeading1, btnHeading2, btnBulletList, btnNumberedList;
    private MaterialButton btnSelectImages;
    private RecyclerView rvSelectedImages;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // Loading UI components
    private View loadingOverlay;
    private com.google.android.material.progressindicator.CircularProgressIndicator progressIndicator;
    private TextView loadingText;

    private List<Uri> selectedImageUris;
    private SelectedImagesAdapter selectedImagesAdapter;
    private List<String> uploadedImageUrls;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    public static AddAnnouncementDialog newInstance() {
        return new AddAnnouncementDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Campus_Dialog_FullScreen);

        // Initialize lists
        selectedImageUris = new ArrayList<>();
        uploadedImageUrls = new ArrayList<>();

        // Initialize activity result launchers
        setupActivityResultLaunchers();
    }

    private void setupActivityResultLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    if (data.getClipData() != null) {
                        // Multiple images selected
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            selectedImageUris.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        // Single image selected
                        selectedImageUris.add(data.getData());
                    }

                    updateSelectedImagesUI();
                }
            }
        );

        // Permission launcher
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(requireContext(), "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_announcement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        initViews(view);
        setupToolbar(view);
        setupVisibilityDropdown();
        setupRichEditor();
        setupRichEditorToolbar();
        setupImageSelection();
    }

    private void initViews(View view) {
        etTitle = view.findViewById(R.id.etTitle);
        etDepartment = view.findViewById(R.id.etDepartment);
        actvVisibility = view.findViewById(R.id.actvVisibility);
        richEditor = view.findViewById(R.id.richEditor);
        btnSelectImages = view.findViewById(R.id.btnSelectImages);
        rvSelectedImages = view.findViewById(R.id.rvSelectedImages);

        // Rich editor toolbar buttons
        btnBold = view.findViewById(R.id.btnBold);
        btnItalic = view.findViewById(R.id.btnItalic);
        btnUnderline = view.findViewById(R.id.btnUnderline);
        btnHeading1 = view.findViewById(R.id.btnHeading1);
        btnHeading2 = view.findViewById(R.id.btnHeading2);
        btnBulletList = view.findViewById(R.id.btnBulletList);
        btnNumberedList = view.findViewById(R.id.btnNumberedList);

        // Loading UI components
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        loadingText = view.findViewById(R.id.loadingText);
    }

    private void setupImageSelection() {
        // Setup RecyclerView for selected images
        selectedImagesAdapter = new SelectedImagesAdapter(selectedImageUris, this);
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSelectedImages.setAdapter(selectedImagesAdapter);

        // Setup image selection button
        btnSelectImages.setOnClickListener(v -> checkPermissionAndOpenPicker());
    }

    private void checkPermissionAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= 34) { // API 34 (Android 14+)
            // Android 14+ - Handle partial photo access
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                // Full access granted
                openImagePicker();
            } else if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED) {
                // Partial access granted
                openImagePicker();
            } else {
                // Request READ_MEDIA_IMAGES first, fallback to partial access if denied
                permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33 (Android 13)
            // Android 13 - Use READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 and below - Use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

    private void updateSelectedImagesUI() {
        if (selectedImageUris.isEmpty()) {
            rvSelectedImages.setVisibility(View.GONE);
            btnSelectImages.setText("Select Images");
        } else {
            rvSelectedImages.setVisibility(View.VISIBLE);
            btnSelectImages.setText("Add More Images (" + selectedImageUris.size() + ")");
            selectedImagesAdapter.updateImages(selectedImageUris);
        }
    }

    @Override
    public void onImageRemove(int position) {
        selectedImageUris.remove(position);
        updateSelectedImagesUI();
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveAnnouncement();
                return true;
            }
            return false;
        });
    }

    private void setupVisibilityDropdown() {
        String[] visibilityOptions = {"All Students", "Faculty Only", "Staff Only", "Department Only", "Year 1", "Year 2", "Year 3", "Year 4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, visibilityOptions);
        actvVisibility.setAdapter(adapter);
        actvVisibility.setText("All Students", false);
    }

    private void setupRichEditor() {
        richEditor.setEditorHeight(200);
        richEditor.setEditorFontSize(16);
        richEditor.setPadding(10, 10, 10, 10);
        richEditor.setPlaceholder("Write your announcement content here...");
        richEditor.setInputEnabled(true);

        // Set up JavaScript interface to track formatting changes
        richEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                // Update button states when text changes
                updateFormattingButtonStates();
            }
        });

        // Enable JavaScript callbacks for formatting state detection
        richEditor.setOnDecorationChangeListener(new RichEditor.OnDecorationStateListener() {
            @Override
            public void onStateChangeListener(String text, List<RichEditor.Type> types) {
                updateButtonStatesFromTypes(types);
            }
        });
    }

    private void setupRichEditorToolbar() {
        btnBold.setOnClickListener(v -> {
            richEditor.setBold();
            toggleButtonState(btnBold);
        });

        btnItalic.setOnClickListener(v -> {
            richEditor.setItalic();
            toggleButtonState(btnItalic);
        });

        btnUnderline.setOnClickListener(v -> {
            richEditor.setUnderline();
            toggleButtonState(btnUnderline);
        });

        btnHeading1.setOnClickListener(v -> {
            richEditor.setHeading(1);
            // Clear other heading states
            setButtonActive(btnHeading2, false);
            toggleButtonState(btnHeading1);
        });

        btnHeading2.setOnClickListener(v -> {
            richEditor.setHeading(2);
            // Clear other heading states
            setButtonActive(btnHeading1, false);
            toggleButtonState(btnHeading2);
        });

        btnBulletList.setOnClickListener(v -> {
            richEditor.setBullets();
            // Clear numbered list state
            setButtonActive(btnNumberedList, false);
            toggleButtonState(btnBulletList);
        });

        btnNumberedList.setOnClickListener(v -> {
            richEditor.setNumbers();
            // Clear bullet list state
            setButtonActive(btnBulletList, false);
            toggleButtonState(btnNumberedList);
        });
    }

    private void toggleButtonState(MaterialButton button) {
        boolean isSelected = button.isSelected();
        setButtonActive(button, !isSelected);
    }

    private void setButtonActive(MaterialButton button, boolean active) {
        button.setSelected(active);
        // Update visual appearance
        if (active) {
            button.setIconTint(getResources().getColorStateList(android.R.color.white, null));
            button.setTextColor(getResources().getColor(android.R.color.white, null));
        } else {
            button.setIconTint(getResources().getColorStateList(R.color.md_theme_onSurfaceVariant, null));
            button.setTextColor(getResources().getColor(R.color.md_theme_onSurfaceVariant, null));
        }
    }

    private void updateFormattingButtonStates() {
        // This method will be called when text changes
        // We can add logic here to detect current formatting if needed
    }

    private void updateButtonStatesFromTypes(List<RichEditor.Type> types) {
        // Reset all button states
        setButtonActive(btnBold, false);
        setButtonActive(btnItalic, false);
        setButtonActive(btnUnderline, false);
        setButtonActive(btnHeading1, false);
        setButtonActive(btnHeading2, false);
        setButtonActive(btnBulletList, false);
        setButtonActive(btnNumberedList, false);

        // Set active states based on current formatting
        if (types != null) {
            for (RichEditor.Type type : types) {
                switch (type) {
                    case BOLD:
                        setButtonActive(btnBold, true);
                        break;
                    case ITALIC:
                        setButtonActive(btnItalic, true);
                        break;
                    case UNDERLINE:
                        setButtonActive(btnUnderline, true);
                        break;
                    case H1:
                        setButtonActive(btnHeading1, true);
                        break;
                    case H2:
                        setButtonActive(btnHeading2, true);
                        break;
                    case UNORDEREDLIST:
                        setButtonActive(btnBulletList, true);
                        break;
                    case ORDEREDLIST:
                        setButtonActive(btnNumberedList, true);
                        break;
                }
            }
        }
    }

    private void saveAnnouncement() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String department = etDepartment.getText() != null ? etDepartment.getText().toString().trim() : "";
        String body = richEditor.getHtml();
        String visibility = actvVisibility.getText().toString();

        // Validate required fields
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (department.isEmpty()) {
            etDepartment.setError("Department is required");
            etDepartment.requestFocus();
            return;
        }

        if (body == null || body.trim().isEmpty() || body.equals("<p><br></p>")) {
            Toast.makeText(requireContext(), "Announcement content is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading UI
        showLoading();

        // Upload images to Firebase Storage and get URLs
        List<String> imageUrls = new ArrayList<>();
        if (!selectedImageUris.isEmpty()) {
            updateLoadingText("Uploading images...");

            for (Uri imageUri : selectedImageUris) {
                // Create a reference to store the image using a UUID to avoid duplicates
                StorageReference imageRef = storage.getReference().child("announcement_images/" + UUID.randomUUID().toString());

                // Upload the image
                imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL and add to the list
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageUrls.add(uri.toString());

                            // Check if all images are uploaded
                            if (imageUrls.size() == selectedImageUris.size()) {
                                // All images uploaded, now save announcement
                                saveAnnouncementToFirestore(title, department, body, visibility, imageUrls);
                            }
                        }).addOnFailureListener(e -> {
                            hideLoading();
                            System.err.println("Image URL Error: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        hideLoading();
                        System.err.println("Image Upload Error: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            }
        } else {
            // No images selected, create announcement without images
            saveAnnouncementToFirestore(title, department, body, visibility, imageUrls);
        }
    }

    private void saveAnnouncementToFirestore(String title, String department, String body, String visibility, List<String> imageUrls) {
        updateLoadingText("Saving announcement...");

        List<String> visibilityList = new ArrayList<>();
        visibilityList.add(visibility);

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setDepartment(department);
        announcement.setBody(body);
        announcement.setVisibility(visibilityList);
        announcement.setImageUrls(imageUrls);
        announcement.setCreatedAt(new Date());

        db.collection("announcement")
                .add(announcement)
                .addOnSuccessListener(documentReference -> {
                    hideLoading();
                    System.out.println("Firestore Success: Document added with ID: " + documentReference.getId());
                    Toast.makeText(requireContext(), "Announcement created successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    System.err.println("Firestore Error: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Failed to create announcement: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading() {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingText.setText("Creating announcement...");
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }

    private void updateLoadingText(String text) {
        loadingText.setText(text);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        return dialog;
    }
}
