package com.group.campus.models;

public class Reply {
    private String replyId;
    private String replierId;
    private String text;
    private long timestamp;

    public Reply() {
        // Default constructor required for Firebase
    }

    public Reply(String replyId, String replierId, String text, long timestamp) {
        this.replyId = replyId;
        this.replierId = replierId;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters
    public String getReplyId() {
        return replyId;
    }

    public String getReplierId() {
        return replierId;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public void setReplierId(String replierId) {
        this.replierId = replierId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
