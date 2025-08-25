package com.group.campus.models;

import java.util.ArrayList;

public class Suggestion {
    private String suggestionId;
    private String senderId;
    private String receiverId; // optional when using department routing
    private String receiverDepartment; // new: department routing
    private String text;
    private boolean isAnonymous;
    private long timestamp;
    private ArrayList<String> files;
    private ArrayList<Reply> replies;

    public Suggestion() {
        // Default constructor required for Firebase
        this.files = new ArrayList<>();
        this.replies = new ArrayList<>();
    }

    public Suggestion(String suggestionId, String senderId, String receiverId, String text,
                     boolean isAnonymous, long timestamp) {
        this.suggestionId = suggestionId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.isAnonymous = isAnonymous;
        this.timestamp = timestamp;
        this.files = new ArrayList<>();
        this.replies = new ArrayList<>();
    }

    // Convenience ctor for department routing
    public Suggestion(String suggestionId, String senderId, String receiverDepartment, String text,
                      boolean isAnonymous, long timestamp, boolean byDepartment) {
        this.suggestionId = suggestionId;
        this.senderId = senderId;
        this.receiverDepartment = receiverDepartment;
        this.text = text;
        this.isAnonymous = isAnonymous;
        this.timestamp = timestamp;
        this.files = new ArrayList<>();
        this.replies = new ArrayList<>();
    }

    // Getters and setters
    public String getSuggestionId() { return suggestionId; }
    public void setSuggestionId(String suggestionId) { this.suggestionId = suggestionId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverDepartment() { return receiverDepartment; }
    public void setReceiverDepartment(String receiverDepartment) { this.receiverDepartment = receiverDepartment; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public ArrayList<String> getFiles() { return files; }
    public void setFiles(ArrayList<String> files) { this.files = files; }

    public ArrayList<Reply> getReplies() { return replies; }
    public void setReplies(ArrayList<Reply> replies) { this.replies = replies; }

    public void addFile(String fileUrl) {
        if (this.files == null) this.files = new ArrayList<>();
        this.files.add(fileUrl);
    }

    public void addReply(Reply reply) {
        if (this.replies == null) this.replies = new ArrayList<>();
        this.replies.add(reply);
    }
}
