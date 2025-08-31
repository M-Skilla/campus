package com.group.campus.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Suggestion {
    private String suggestionId;
    private String senderId;
    private String senderName;
    private String senderRegNo;
    private String receiverId; // optional when using department routing
    private String receiverDepartment; // department routing (Health, Facilities, Library)
    private String subject;
    private String text;
    private boolean isAnonymous;
    private long timestamp;
    private String status; // "open", "replied", "closed"
    private List<String> attachmentUrls;
    private List<Map<String, Object>> attachments; // Enhanced attachment info
    private List<Reply> replies;
    private int replyCount;

    public Suggestion() {
        // Default constructor required for Firebase
        this.attachmentUrls = new ArrayList<>();
        this.attachments = new ArrayList<>();
        this.replies = new ArrayList<>();
        this.status = "open";
        this.replyCount = 0;
    }

    public Suggestion(String suggestionId, String senderId, String senderName, String senderRegNo,
                     String receiverDepartment, String subject, String text,
                     boolean isAnonymous, long timestamp) {
        this.suggestionId = suggestionId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRegNo = senderRegNo;
        this.receiverDepartment = receiverDepartment;
        this.subject = subject;
        this.text = text;
        this.isAnonymous = isAnonymous;
        this.timestamp = timestamp;
        this.attachmentUrls = new ArrayList<>();
        this.attachments = new ArrayList<>();
        this.replies = new ArrayList<>();
        this.status = "open";
        this.replyCount = 0;
    }

    // Enhanced attachment support
    public void addAttachment(String fileName, String fileUrl, String fileType, long fileSize) {
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("fileName", fileName);
        attachment.put("fileUrl", fileUrl);
        attachment.put("fileType", fileType);
        attachment.put("fileSize", fileSize);
        attachment.put("uploadTime", System.currentTimeMillis());

        this.attachments.add(attachment);
        this.attachmentUrls.add(fileUrl);
    }

    // Getters and setters
    public String getSuggestionId() { return suggestionId; }
    public void setSuggestionId(String suggestionId) { this.suggestionId = suggestionId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderRegNo() { return senderRegNo; }
    public void setSenderRegNo(String senderRegNo) { this.senderRegNo = senderRegNo; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverDepartment() { return receiverDepartment; }
    public void setReceiverDepartment(String receiverDepartment) { this.receiverDepartment = receiverDepartment; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(List<String> attachmentUrls) { this.attachmentUrls = attachmentUrls; }

    public List<Map<String, Object>> getAttachments() { return attachments; }
    public void setAttachments(List<Map<String, Object>> attachments) { this.attachments = attachments; }

    public List<Reply> getReplies() { return replies; }
    public void setReplies(List<Reply> replies) {
        this.replies = replies;
        this.replyCount = replies != null ? replies.size() : 0;
    }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public static class Reply {
        private String replyId;
        private String senderId;
        private String senderName;
        private String senderRegNo;
        private String text;
        private long timestamp;
        private List<Map<String, Object>> attachments;

        public Reply() {
            this.attachments = new ArrayList<>();
        }

        public Reply(String replyId, String senderId, String senderName, String senderRegNo,
                    String text, long timestamp) {
            this.replyId = replyId;
            this.senderId = senderId;
            this.senderName = senderName;
            this.senderRegNo = senderRegNo;
            this.text = text;
            this.timestamp = timestamp;
            this.attachments = new ArrayList<>();
        }

        // Getters and setters for Reply
        public String getReplyId() { return replyId; }
        public void setReplyId(String replyId) { this.replyId = replyId; }

        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }

        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }

        public String getSenderRegNo() { return senderRegNo; }
        public void setSenderRegNo(String senderRegNo) { this.senderRegNo = senderRegNo; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public List<Map<String, Object>> getAttachments() { return attachments; }
        public void setAttachments(List<Map<String, Object>> attachments) { this.attachments = attachments; }
    }

    // Additional utility methods needed by adapters
    public String getId() { return suggestionId; }
    public void setId(String id) { this.suggestionId = id; }

    public String getAuthorName() { return senderName; }
    public String getAuthorId() { return senderId; }

    public String getDepartment() { return receiverDepartment; }
    public void setDepartment(String department) { this.receiverDepartment = department; }

    public boolean isOutgoing() {
        // Determine if this is an outgoing message - can be based on current user context
        // For now, return false as default (incoming) - this should be set by the calling code
        return false;
    }

    public boolean hasAttachment() {
        return attachmentUrls != null && !attachmentUrls.isEmpty();
    }

    public String getAttachmentUrl() {
        return (attachmentUrls != null && !attachmentUrls.isEmpty()) ? attachmentUrls.get(0) : null;
    }

    public String getAttachmentType() {
        if (attachments != null && !attachments.isEmpty()) {
            Map<String, Object> firstAttachment = attachments.get(0);
            return (String) firstAttachment.get("fileType");
        }
        return null;
    }

    public String getFormattedTime() {
        // Simple time formatting - you can enhance this with proper date formatting
        return android.text.format.DateFormat.format("HH:mm", timestamp).toString();
    }

    public String getAvatarInitials() {
        if (senderName != null && !senderName.isEmpty()) {
            String[] parts = senderName.split("\\s+");
            if (parts.length >= 2) {
                return "" + parts[0].charAt(0) + parts[1].charAt(0);
            } else {
                return "" + senderName.charAt(0);
            }
        }
        return "?";
    }

    // Status enum for compatibility with adapters
    public enum SuggestionStatus {
        SENDING, SENT, DELIVERED, READ, FAILED
    }

    public SuggestionStatus getStatusEnum() {
        // Convert string status to enum
        if (status == null) return SuggestionStatus.SENT;

        switch (status.toLowerCase()) {
            case "sending": return SuggestionStatus.SENDING;
            case "sent": return SuggestionStatus.SENT;
            case "delivered": return SuggestionStatus.DELIVERED;
            case "read": return SuggestionStatus.READ;
            case "failed": return SuggestionStatus.FAILED;
            default: return SuggestionStatus.SENT;
        }
    }
}
