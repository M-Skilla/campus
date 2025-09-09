package com.group.campus.service;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.group.campus.models.Suggestion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Enhanced service class for managing suggestions with proper attachment support
 */
public class SuggestionsService {

    private static final String TAG = "SuggestionsService";
    private static final String SUGGESTIONS_COLLECTION = "suggestions";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;

    public interface SuggestionCallback {
        void onSuccess(String message);
        void onError(Exception error);
    }

    public interface SuggestionsListCallback {
        void onSuggestionsReceived(List<Suggestion> suggestions);
        void onError(Exception error);
    }

    public interface MessageUploadCallback {
        void onSuccess(String suggestionId);
        void onError(Exception error);
    }

    public interface ConversationListener {
        void onConversationsChanged(List<Suggestion> suggestions);
        void onError(Exception error);
    }

    public interface MessageListener {
        void onMessagesChanged(List<Suggestion.Reply> messages);
        void onError(Exception error);
    }

    public SuggestionsService() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Send a suggestion with attachments support
     */
    public void sendSuggestion(String department, String subject, String messageText,
                              List<Uri> attachments, boolean isAnonymous, MessageUploadCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String suggestionId = UUID.randomUUID().toString();

        // Get user info first
        db.collection(USERS_COLLECTION).document(userId)
            .get()
            .addOnSuccessListener(userDoc -> {
                if (userDoc.exists()) {
                    String senderName = userDoc.getString("fullName");
                    String senderRegNo = userDoc.getString("regNo");

                    // Create suggestion object
                    Suggestion suggestion = new Suggestion(
                        suggestionId, userId, senderName, senderRegNo,
                        department, subject, messageText, isAnonymous, System.currentTimeMillis()
                    );

                    // Upload attachments if any
                    if (attachments != null && !attachments.isEmpty()) {
                        uploadAttachments(suggestionId, attachments, (attachmentUrls) -> {
                            // Add attachments to suggestion
                            for (String url : attachmentUrls) {
                                suggestion.addAttachment("attachment", url, "file", 0);
                            }
                            saveSuggestion(suggestion, callback);
                        }, new MessageUploadCallback() {
                            @Override
                            public void onSuccess(String suggestionId) {
                                // not used for error handling here
                            }

                            @Override
                            public void onError(Exception error) {
                                callback.onError(error);
                            }
                        });
                    } else {
                        saveSuggestion(suggestion, callback);
                    }
                } else {
                    callback.onError(new Exception("User data not found"));
                }
            })
            .addOnFailureListener(error -> callback.onError(error));
    }

    /**
     * Upload attachments to Firebase Storage
     */
    private void uploadAttachments(String suggestionId, List<Uri> attachments,
                                  AttachmentUploadCallback callback, MessageUploadCallback errorCallback) {
        List<String> uploadedUrls = new ArrayList<>();
        int totalAttachments = attachments.size();

        for (int i = 0; i < attachments.size(); i++) {
            Uri attachment = attachments.get(i);
            String fileName = "attachment_" + i + "_" + System.currentTimeMillis();
            StorageReference ref = storage.getReference()
                .child("suggestions")
                .child(suggestionId)
                .child(fileName);

            ref.putFile(attachment)
                .addOnSuccessListener(taskSnapshot ->
                    ref.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        uploadedUrls.add(downloadUrl.toString());
                        if (uploadedUrls.size() == totalAttachments) {
                            callback.onAttachmentsUploaded(uploadedUrls);
                        }
                    }).addOnFailureListener(error -> errorCallback.onError(error))
                )
                .addOnFailureListener(error -> errorCallback.onError(error));
        }
    }

    private interface AttachmentUploadCallback {
        void onAttachmentsUploaded(List<String> urls);
    }

    public interface OperationCallback {
        void onSuccess(String result);
        void onError(Exception error);
    }

    /**
     * Listen to staff conversations for a department - FIXED VERSION
     */
    public ListenerRegistration listenToStaffConversations(String department, ConversationListener listener) {
        // Simplified query with better error handling
        return db.collection(SUGGESTIONS_COLLECTION)
            .whereEqualTo("receiverDepartment", department)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error in listenToStaffConversations", error);
                    listener.onError(error);
                    return;
                }

                if (snapshots != null) {
                    List<Suggestion> suggestions = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            Suggestion suggestion = doc.toObject(Suggestion.class);
                            if (suggestion != null) {
                                suggestions.add(suggestion);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing suggestion document", e);
                            // Skip invalid documents
                        }
                    }
                    listener.onConversationsChanged(suggestions);
                } else {
                    listener.onConversationsChanged(new ArrayList<>());
                }
            });
    }

    /**
     * Listen to user inbox (suggestions with replies) - FIXED VERSION
     */
    public ListenerRegistration listenToUserInbox(String userId, ConversationListener listener) {
        // Simplified query - removed composite orderBy that requires indexes
        return db.collection(SUGGESTIONS_COLLECTION)
            .whereEqualTo("senderId", userId)
            .whereGreaterThan("replyCount", 0)
            .orderBy("replyCount")
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error in listenToUserInbox", error);
                    listener.onError(error);
                    return;
                }

                if (snapshots != null) {
                    List<Suggestion> suggestions = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            Suggestion suggestion = doc.toObject(Suggestion.class);
                            if (suggestion != null && suggestion.getReplyCount() > 0) {
                                suggestions.add(suggestion);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing suggestion document", e);
                            // Skip invalid documents
                        }
                    }
                    listener.onConversationsChanged(suggestions);
                } else {
                    listener.onConversationsChanged(new ArrayList<>());
                }
            });
    }

    /**
     * Listen to user outbox (sent suggestions) - FIXED VERSION
     */
    public ListenerRegistration listenToUserOutbox(String userId, ConversationListener listener) {
        // Added better error handling and logging
        return db.collection(SUGGESTIONS_COLLECTION)
            .whereEqualTo("senderId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error in listenToUserOutbox", error);
                    listener.onError(error);
                    return;
                }

                if (snapshots != null) {
                    List<Suggestion> suggestions = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            Suggestion suggestion = doc.toObject(Suggestion.class);
                            if (suggestion != null) {
                                suggestions.add(suggestion);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing suggestion document", e);
                            // Skip invalid documents
                        }
                    }
                    listener.onConversationsChanged(suggestions);
                } else {
                    listener.onConversationsChanged(new ArrayList<>());
                }
            });
    }

    /**
     * Listen to messages in a conversation/suggestion
     * Now returns a list whose first element is a synthetic Reply representing the original suggestion message.
     */
    public ListenerRegistration listenToMessages(String suggestionId, MessageListener listener) {
        return db.collection(SUGGESTIONS_COLLECTION)
            .document(suggestionId)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    listener.onError(error);
                    return;
                }
                try {
                    if (snapshot != null && snapshot.exists()) {
                        Suggestion suggestion = snapshot.toObject(Suggestion.class);
                        if (suggestion != null) {
                            List<Suggestion.Reply> combined = new ArrayList<>();
                            Suggestion.Reply root = new Suggestion.Reply();
                            root.setReplyId((suggestion.getSuggestionId() != null ? suggestion.getSuggestionId() : suggestionId) + "_root");
                            root.setSenderId(suggestion.getSenderId());
                            root.setSenderName(suggestion.getSenderName() != null ? suggestion.getSenderName() : "User");
                            root.setSenderRegNo(suggestion.getSenderRegNo());
                            root.setText(suggestion.getText() != null ? suggestion.getText() : "");
                            root.setTimestamp(suggestion.getTimestamp());
                            combined.add(root);
                            if (suggestion.getReplies() != null) {
                                combined.addAll(suggestion.getReplies());
                            }
                            listener.onMessagesChanged(combined);
                        } else {
                            listener.onMessagesChanged(new ArrayList<>());
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "listenToMessages parse failure", ex);
                    listener.onError(ex);
                }
            });
    }

    /**
     * Create a new conversation/suggestion
     */
    public void createConversation(String department, String subject, String messageText, OperationCallback callback) {
        sendSuggestion(department, subject, messageText, null, false, new MessageUploadCallback() {
            @Override
            public void onSuccess(String suggestionId) {
                callback.onSuccess(suggestionId);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Upload attachment for a conversation
     */
    public void uploadAttachment(String suggestionId, Uri attachmentUri, String fileName, OperationCallback callback) {
        StorageReference ref = storage.getReference()
            .child("suggestions")
            .child(suggestionId)
            .child(fileName);

        ref.putFile(attachmentUri)
            .addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    callback.onSuccess(downloadUrl.toString());
                }).addOnFailureListener(error -> callback.onError(error));
            })
            .addOnFailureListener(error -> callback.onError(error));
    }

    /**
     * Add message as staff member
     */
    public void addMessageAsStaff(String suggestionId, String messageText, String attachmentUrl, OperationCallback callback) {
        replyToSuggestion(suggestionId, messageText, null, new SuggestionCallback() {
            @Override
            public void onSuccess(String message) {
                callback.onSuccess(message);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Add regular message to conversation
     */
    public void addMessage(String suggestionId, String messageText, String attachmentUrl, OperationCallback callback) {
        // For now, treat this the same as staff reply since we're using a simpler model
        replyToSuggestion(suggestionId, messageText, null, new SuggestionCallback() {
            @Override
            public void onSuccess(String message) {
                callback.onSuccess(message);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Save suggestion to Firestore
     */
    private void saveSuggestion(Suggestion suggestion, MessageUploadCallback callback) {
        db.collection(SUGGESTIONS_COLLECTION)
            .document(suggestion.getSuggestionId())
            .set(suggestion)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Suggestion saved successfully: " + suggestion.getSuggestionId());
                callback.onSuccess(suggestion.getSuggestionId());
            })
            .addOnFailureListener(error -> {
                Log.e(TAG, "Error saving suggestion", error);
                callback.onError(error);
            });
    }

    /**
     * Get suggestions sent by current user (for Outbox)
     */
    public void getUserSentSuggestions(SuggestionsListCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection(SUGGESTIONS_COLLECTION)
            .whereEqualTo("senderId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Suggestion> suggestions = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Suggestion suggestion = doc.toObject(Suggestion.class);
                    if (suggestion != null) {
                        suggestions.add(suggestion);
                    }
                }
                callback.onSuggestionsReceived(suggestions);
            })
            .addOnFailureListener(callback::onError);
    }

    /**
     * Get suggestions with replies for current user (for Inbox)
     */
    public void getUserInboxSuggestions(SuggestionsListCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection(SUGGESTIONS_COLLECTION)
            .whereEqualTo("senderId", userId)
            .whereGreaterThan("replyCount", 0)
            .orderBy("replyCount")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Suggestion> suggestionsWithReplies = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Suggestion suggestion = doc.toObject(Suggestion.class);
                    if (suggestion != null && suggestion.getReplyCount() > 0) {
                        suggestionsWithReplies.add(suggestion);
                    }
                }
                callback.onSuggestionsReceived(suggestionsWithReplies);
            })
            .addOnFailureListener(callback::onError);
    }

    /**
     * Get suggestions for staff dashboard (filtered by department)
     */
    public void getStaffDepartmentSuggestions(String department, SuggestionsListCallback callback) {
        db.collection(SUGGESTIONS_COLLECTION)
            .whereEqualTo("receiverDepartment", department)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Suggestion> suggestions = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Suggestion suggestion = doc.toObject(Suggestion.class);
                    if (suggestion != null) {
                        suggestions.add(suggestion);
                    }
                }
                callback.onSuggestionsReceived(suggestions);
            })
            .addOnFailureListener(callback::onError);
    }

    /**
     * Reply to a suggestion (for staff)
     */
    public void replyToSuggestion(String suggestionId, String replyText, List<Uri> attachments, SuggestionCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Get current user info and suggestion
        db.collection(USERS_COLLECTION).document(userId)
            .get()
            .addOnSuccessListener(userDoc -> {
                if (userDoc.exists()) {
                    String senderName = userDoc.getString("fullName");
                    String senderRegNo = userDoc.getString("regNo");

                    // Create reply
                    Suggestion.Reply reply = new Suggestion.Reply(
                        UUID.randomUUID().toString(),
                        userId, senderName, senderRegNo,
                        replyText, System.currentTimeMillis()
                    );

                    // Update suggestion with reply
                    db.collection(SUGGESTIONS_COLLECTION).document(suggestionId)
                        .get()
                        .addOnSuccessListener(suggestionDoc -> {
                            if (suggestionDoc.exists()) {
                                Suggestion suggestion = suggestionDoc.toObject(Suggestion.class);
                                if (suggestion != null) {
                                    if (suggestion.getReplies() == null) {
                                        suggestion.setReplies(new ArrayList<>());
                                    }
                                    suggestion.getReplies().add(reply);
                                    suggestion.setReplyCount(suggestion.getReplies().size());
                                    suggestion.setStatus("replied");

                                    // Save updated suggestion
                                    db.collection(SUGGESTIONS_COLLECTION).document(suggestionId)
                                        .set(suggestion)
                                        .addOnSuccessListener(aVoid -> callback.onSuccess("Reply sent successfully"))
                                        .addOnFailureListener(error -> callback.onError(error));
                                }
                            } else {
                                callback.onError(new Exception("Suggestion not found"));
                            }
                        })
                        .addOnFailureListener(callback::onError);
                } else {
                    callback.onError(new Exception("User data not found"));
                }
            })
            .addOnFailureListener(error -> callback.onError(error));
    }
}
