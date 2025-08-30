package com.group.campus.model;

import com.google.firebase.Timestamp;

public class SuggestionConversation {
    private String id;
    private String studentId;
    private String studentName;
    private String department;
    private String subject;
    private String status; // "open", "resolved"
    private Timestamp createdAt;
    private Timestamp lastMessageAt;
    private String lastMessageText;
    private boolean hasUnreadMessages;

    public SuggestionConversation() {}

    public SuggestionConversation(String studentId, String studentName, String department, String subject) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.subject = subject;
        this.status = "open";
        this.createdAt = Timestamp.now();
        this.lastMessageAt = Timestamp.now();
        this.hasUnreadMessages = false;
        this.lastMessageText = "";
    }

    // Constructor to create SuggestionConversation from Suggestion
    public SuggestionConversation(com.group.campus.models.Suggestion suggestion) {
        this.id = suggestion.getSuggestionId();
        this.studentId = suggestion.getSenderId();
        this.studentName = suggestion.getSenderName();
        this.department = suggestion.getReceiverDepartment();
        this.subject = suggestion.getSubject();
        this.status = suggestion.getStatus();
        this.createdAt = new Timestamp(suggestion.getTimestamp() / 1000, 0);
        this.lastMessageAt = new Timestamp(suggestion.getTimestamp() / 1000, 0);
        this.hasUnreadMessages = suggestion.getReplyCount() > 0;
        this.lastMessageText = suggestion.getText();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Timestamp lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }

    public boolean isHasUnreadMessages() { return hasUnreadMessages; }
    public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }

    // Additional setter methods for proper conversion from Suggestion objects
    public void setMessage(String message) {
        this.lastMessageText = message;
    }

    public void setTimestamp(long timestamp) {
        this.createdAt = new Timestamp(timestamp / 1000, 0);
        this.lastMessageAt = new Timestamp(timestamp / 1000, 0);
    }
}
