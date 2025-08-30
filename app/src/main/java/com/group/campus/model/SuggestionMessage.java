package com.group.campus.model;

import com.google.firebase.Timestamp;

public class SuggestionMessage {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String text;
    private Timestamp timestamp;
    private boolean isFromStaff;
    private String attachmentUrl;

    public SuggestionMessage() {}

    public SuggestionMessage(String conversationId, String senderId, String senderName, String text, boolean isFromStaff) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.isFromStaff = isFromStaff;
        this.timestamp = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isFromStaff() { return isFromStaff; }
    public void setFromStaff(boolean fromStaff) { isFromStaff = fromStaff; }

    public boolean isFromStudent() { return !isFromStaff; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public boolean hasAttachment() { return attachmentUrl != null && !attachmentUrl.isEmpty(); }

    public String getFormattedTime() {
        if (timestamp == null) return "";
        long millis = timestamp.toDate().getTime();
        long diff = System.currentTimeMillis() - millis;
        long minutes = diff / (1000 * 60);
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        long days = hours / 24;
        return days + "d";
    }
}
