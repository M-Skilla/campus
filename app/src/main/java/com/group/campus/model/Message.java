package com.group.campus.model;

import java.util.Date;

public class Message {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String senderType; // "student" or "staff"
    private String text;
    private String attachmentUrl;
    private String attachmentName;
    private String attachmentType;
    private Date timestamp;
    private boolean isRead;

    public Message() {}

    public Message(String conversationId, String senderId, String senderName, String senderType, String text) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderType = senderType;
        this.text = text;
        this.timestamp = new Date();
        this.isRead = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }

    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean hasAttachment() {
        return attachmentUrl != null && !attachmentUrl.isEmpty();
    }

    public boolean isFromStudent() {
        return "student".equals(senderType);
    }

    public boolean isFromStaff() {
        return "staff".equals(senderType);
    }

    public String getFormattedTime() {
        if (timestamp == null) return "";
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return timeFormat.format(timestamp);
    }
}
