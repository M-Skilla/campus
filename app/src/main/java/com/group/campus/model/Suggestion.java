package com.group.campus.model;

import java.util.Date;

public class Suggestion {
    private String id;
    private String text;
    private String authorName;
    private String authorAvatar;
    private Date timestamp;
    private boolean isOutgoing;
    private String attachmentUrl;
    private String attachmentType;
    private SuggestionStatus status;

    public enum SuggestionStatus {
        SENDING, SENT, DELIVERED, READ, FAILED
    }

    public Suggestion() {
        // Empty constructor for Firebase/Gson
    }

    public Suggestion(String text, boolean isOutgoing) {
        this.text = text;
        this.isOutgoing = isOutgoing;
        this.timestamp = new Date();
        this.status = isOutgoing ? SuggestionStatus.SENDING : SuggestionStatus.DELIVERED;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isOutgoing() { return isOutgoing; }
    public void setOutgoing(boolean outgoing) { isOutgoing = outgoing; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }

    public SuggestionStatus getStatus() { return status; }
    public void setStatus(SuggestionStatus status) { this.status = status; }

    public boolean hasAttachment() {
        return attachmentUrl != null && !attachmentUrl.isEmpty();
    }

    public String getFormattedTime() {
        if (timestamp == null) return "";

        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return timeFormat.format(timestamp);
    }

    public String getAvatarInitials() {
        if (authorName == null || authorName.isEmpty()) return "U";

        String[] parts = authorName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else {
            return authorName.substring(0, 1).toUpperCase();
        }
    }
}
