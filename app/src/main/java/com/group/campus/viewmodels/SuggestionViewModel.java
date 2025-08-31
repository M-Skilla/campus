package com.group.campus.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.group.campus.models.Reply;
import com.group.campus.models.Suggestion;
import com.group.campus.models.User;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class SuggestionViewModel extends ViewModel {

    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private FirebaseAuth firebaseAuth;

    private MutableLiveData<List<Suggestion>> suggestionsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<User>> staffUsersLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> uploadProgressLiveData = new MutableLiveData<>();
    private MutableLiveData<String> successMessageLiveData = new MutableLiveData<>();
    private MutableLiveData<List<String>> departmentsLiveData = new MutableLiveData<>();

    // Restricted words for moderation
    private final List<String> restrictedWords = Arrays.asList(
        "badword1", "badword2", "inappropriate", "offensive", "spam"
    );

    public SuggestionViewModel() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        loadStaffUsers();
        loadSuggestions();
        loadDepartments();
    }

    // LiveData getters
    public LiveData<List<Suggestion>> getSuggestionsLiveData() {
        return suggestionsLiveData;
    }

    public LiveData<List<User>> getStaffUsersLiveData() {
        return staffUsersLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<Boolean> getUploadProgressLiveData() {
        return uploadProgressLiveData;
    }

    public LiveData<String> getSuccessMessageLiveData() {
        return successMessageLiveData;
    }

    public LiveData<List<String>> getDepartmentsLiveData() { return departmentsLiveData; }

    // Load staff users for receiver selection
    public void loadStaffUsers() {
        loadingLiveData.setValue(true);
        databaseRef.child("users").orderByChild("role").equalTo("staff")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<User> staffList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            staffList.add(user);
                        }
                    }
                    staffUsersLiveData.setValue(staffList);
                    loadingLiveData.setValue(false);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    errorMessageLiveData.setValue("Failed to load staff members: " + databaseError.getMessage());
                    loadingLiveData.setValue(false);
                }
            });
    }

    // Load suggestions based on user role
    public void loadSuggestions() {
        if (firebaseAuth.getCurrentUser() == null) {
            errorMessageLiveData.setValue("User not authenticated");
            return;
        }

        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        loadingLiveData.setValue(true);

        // Get current user's role first
        databaseRef.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    loadSuggestionsForUser(currentUser);
                }
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                errorMessageLiveData.setValue("Failed to load user data: " + databaseError.getMessage());
                loadingLiveData.setValue(false);
            }
        });
    }

    private void loadSuggestionsForUser(User currentUser) {
        String currentUserId = currentUser.getUserId();

        databaseRef.child("suggestions").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Suggestion suggestion = dataSnapshot.getValue(Suggestion.class);
                if (suggestion != null && shouldShowSuggestion(suggestion, currentUser)) {
                    List<Suggestion> currentList = suggestionsLiveData.getValue();
                    if (currentList == null) {
                        currentList = new ArrayList<>();
                    }
                    currentList.add(suggestion);
                    suggestionsLiveData.setValue(currentList);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Suggestion updatedSuggestion = dataSnapshot.getValue(Suggestion.class);
                if (updatedSuggestion != null) {
                    List<Suggestion> currentList = suggestionsLiveData.getValue();
                    if (currentList != null) {
                        for (int i = 0; i < currentList.size(); i++) {
                            if (currentList.get(i).getSuggestionId().equals(updatedSuggestion.getSuggestionId())) {
                                currentList.set(i, updatedSuggestion);
                                break;
                            }
                        }
                        suggestionsLiveData.setValue(currentList);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Handle removal if needed
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle move if needed
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                errorMessageLiveData.setValue("Failed to load suggestions: " + databaseError.getMessage());
            }
        });
    }

    private boolean shouldShowSuggestion(Suggestion suggestion, User currentUser) {
        String currentUserId = currentUser.getUserId();
        String userRole = currentUser.getRole();

        if ("staff".equals(userRole)) {
            // Staff sees suggestions sent directly to them or to their department
            boolean direct = currentUserId.equals(suggestion.getReceiverId());
            boolean byDept = suggestion.getReceiverDepartment() != null &&
                    suggestion.getReceiverDepartment().equals(currentUser.getDepartment());
            return direct || byDept;
        } else {
            // Students see suggestions they sent
            return currentUserId.equals(suggestion.getSenderId());
        }
    }

    // Submit a new suggestion
    public void submitSuggestion(String text, String receiverId, boolean isAnonymous,
                               List<Uri> fileUris, SubmissionCallback callback) {

        if (!validateSuggestionText(text)) {
            errorMessageLiveData.setValue("Suggestion contains inappropriate content");
            return;
        }

        if (firebaseAuth.getCurrentUser() == null) {
            errorMessageLiveData.setValue("User not authenticated");
            return;
        }

        uploadProgressLiveData.setValue(true);
        String suggestionId = databaseRef.child("suggestions").push().getKey();
        String senderId = firebaseAuth.getCurrentUser().getUid();

        if (fileUris != null && !fileUris.isEmpty()) {
            uploadFiles(fileUris, suggestionId, new FileUploadCallback() {
                @Override
                public void onSuccess(ArrayList<String> fileUrls) {
                    createSuggestion(suggestionId, senderId, receiverId, text, isAnonymous, fileUrls, callback);
                }

                @Override
                public void onFailure(String error) {
                    uploadProgressLiveData.setValue(false);
                    errorMessageLiveData.setValue("File upload failed: " + error);
                }
            });
        } else {
            createSuggestion(suggestionId, senderId, receiverId, text, isAnonymous, new ArrayList<>(), callback);
        }
    }

    private void createSuggestion(String suggestionId, String senderId, String receiverId,
                                String text, boolean isAnonymous, ArrayList<String> fileUrls,
                                SubmissionCallback callback) {

        // Get user info first for the new constructor
        FirebaseFirestore.getInstance().collection("users").document(senderId)
            .get()
            .addOnSuccessListener(userDoc -> {
                String senderName = userDoc.getString("fullName");
                String senderRegNo = userDoc.getString("regNo");

                // Use the new Suggestion constructor
                Suggestion suggestion = new Suggestion(
                    suggestionId,
                    senderId,
                    senderName != null ? senderName : "",
                    senderRegNo != null ? senderRegNo : "",
                    receiverId, // department name
                    "Suggestion", // subject
                    text,
                    isAnonymous,
                    System.currentTimeMillis()
                );

                // Add file URLs as attachments
                for (String url : fileUrls) {
                    suggestion.addAttachment("file", url, "file", 0);
                }

                databaseRef.child("suggestions").child(suggestionId).setValue(suggestion)
                    .addOnSuccessListener(aVoid -> {
                        uploadProgressLiveData.setValue(false);
                        successMessageLiveData.setValue("Suggestion submitted successfully");
                        sendNotificationToReceiver(receiverId, text, isAnonymous);
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        uploadProgressLiveData.setValue(false);
                        errorMessageLiveData.setValue("Failed to submit suggestion: " + e.getMessage());
                        if (callback != null) callback.onFailure(e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                uploadProgressLiveData.setValue(false);
                errorMessageLiveData.setValue("Failed to get user info: " + e.getMessage());
                if (callback != null) callback.onFailure(e.getMessage());
            });
    }

    private void uploadFiles(List<Uri> fileUris, String suggestionId, FileUploadCallback callback) {
        ArrayList<String> uploadedUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);
        int totalFiles = fileUris.size();

        for (Uri fileUri : fileUris) {
            String fileName = System.currentTimeMillis() + "_" + uploadCount.get();
            StorageReference fileRef = storageRef.child("suggestions").child(suggestionId).child(fileName);

            fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        uploadedUrls.add(downloadUri.toString());
                        if (uploadCount.incrementAndGet() == totalFiles) {
                            callback.onSuccess(uploadedUrls);
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }
    }

    // Add reply to suggestion
    public void addReply(String suggestionId, String replyText, ReplyCallback callback) {
        if (firebaseAuth.getCurrentUser() == null) {
            errorMessageLiveData.setValue("User not authenticated");
            return;
        }

        String replyId = databaseRef.child("suggestions").child(suggestionId).child("replies").push().getKey();
        String replierId = firebaseAuth.getCurrentUser().getUid();

        Reply reply = new Reply(replyId, replierId, replyText, System.currentTimeMillis());

        databaseRef.child("suggestions").child(suggestionId).child("replies").child(replyId)
            .setValue(reply)
            .addOnSuccessListener(aVoid -> {
                successMessageLiveData.setValue("Reply sent successfully");
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                errorMessageLiveData.setValue("Failed to send reply: " + e.getMessage());
                if (callback != null) callback.onFailure(e.getMessage());
            });
    }

    public void loadDepartments() {
        // Derive unique departments from staff users
        databaseRef.child("users").orderByChild("role").equalTo("staff")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot snapshot) {
                    Set<String> set = new HashSet<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        User u = s.getValue(User.class);
                        if (u != null && u.getDepartment() != null && !u.getDepartment().isEmpty()) {
                            set.add(u.getDepartment());
                        }
                    }
                    departmentsLiveData.setValue(new ArrayList<>(set));
                }
                @Override public void onCancelled(DatabaseError error) { /* ignore */ }
            });
    }

    // Submit a new suggestion to a department
    public void submitSuggestionToDepartment(String text, String department, boolean isAnonymous,
                                             List<Uri> fileUris, SubmissionCallback callback) {
        if (!validateSuggestionText(text)) {
            errorMessageLiveData.setValue("Suggestion contains inappropriate content");
            return;
        }
        if (firebaseAuth.getCurrentUser() == null) {
            errorMessageLiveData.setValue("User not authenticated");
            return;
        }
        uploadProgressLiveData.setValue(true);
        String suggestionId = databaseRef.child("suggestions").push().getKey();
        String senderId = firebaseAuth.getCurrentUser().getUid();

        if (fileUris != null && !fileUris.isEmpty()) {
            uploadFiles(fileUris, suggestionId, new FileUploadCallback() {
                @Override public void onSuccess(ArrayList<String> fileUrls) {
                    createSuggestionForDepartment(suggestionId, senderId, department, text, isAnonymous, fileUrls, callback);
                }
                @Override public void onFailure(String error) {
                    uploadProgressLiveData.setValue(false);
                    errorMessageLiveData.setValue("File upload failed: " + error);
                }
            });
        } else {
            createSuggestionForDepartment(suggestionId, senderId, department, text, isAnonymous, new ArrayList<>(), callback);
        }
    }

    private void createSuggestionForDepartment(String suggestionId, String senderId, String department,
                                               String text, boolean isAnonymous, ArrayList<String> fileUrls,
                                               SubmissionCallback callback) {

        // Get user info first for the new constructor
        FirebaseFirestore.getInstance().collection("users").document(senderId)
            .get()
            .addOnSuccessListener(userDoc -> {
                String senderName = userDoc.getString("fullName");
                String senderRegNo = userDoc.getString("regNo");

                // Use the new Suggestion constructor with department routing
                Suggestion suggestion = new Suggestion(
                    suggestionId,
                    senderId,
                    senderName != null ? senderName : "",
                    senderRegNo != null ? senderRegNo : "",
                    department, // receiver department
                    "Suggestion", // subject
                    text,
                    isAnonymous,
                    System.currentTimeMillis()
                );

                // Add file URLs as attachments
                for (String url : fileUrls) {
                    suggestion.addAttachment("file", url, "file", 0);
                }

                databaseRef.child("suggestions").child(suggestionId).setValue(suggestion)
                    .addOnSuccessListener(aVoid -> {
                        uploadProgressLiveData.setValue(false);
                        successMessageLiveData.setValue("Suggestion submitted successfully");
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        uploadProgressLiveData.setValue(false);
                        errorMessageLiveData.setValue("Failed to submit suggestion: " + e.getMessage());
                        if (callback != null) callback.onFailure(e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                uploadProgressLiveData.setValue(false);
                errorMessageLiveData.setValue("Failed to get user info: " + e.getMessage());
                if (callback != null) callback.onFailure(e.getMessage());
            });
    }

    private boolean validateSuggestionText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        for (String restrictedWord : restrictedWords) {
            if (lowerText.contains(restrictedWord.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private void sendNotificationToReceiver(String receiverId, String suggestionText, boolean isAnonymous) {
        // Get receiver's FCM token and send notification
        databaseRef.child("users").child(receiverId).child("fcmToken")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String fcmToken = dataSnapshot.getValue(String.class);
                    if (fcmToken != null) {
                        // Here you would typically send the notification via your backend
                        // For now, we'll just log it
                        String senderInfo = isAnonymous ? "Anonymous" : "A student";
                        String notificationTitle = "New Suggestion";
                        String notificationBody = senderInfo + " sent you a suggestion";
                        // TODO: Implement FCM notification sending
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });
    }

    // Callback interfaces
    public interface SubmissionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface FileUploadCallback {
        void onSuccess(ArrayList<String> fileUrls);
        void onFailure(String error);
    }

    public interface ReplyCallback {
        void onSuccess();
        void onFailure(String error);
    }
}
