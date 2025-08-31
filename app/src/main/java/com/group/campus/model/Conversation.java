package com.group.campus.model;

import java.util.Date;

public class Conversation {
    private String id;
    private String studentId;
    private String studentName;
    private String department;
    private String subject;
    private String status; // "open", "resolved"
    private Date createdAt;
    private Date lastMessageAt;
    private String lastMessageText;
    private boolean hasUnreadMessages;

    public Conversation() {}

    public Conversation(String studentId, String studentName, String department, String subject) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.subject = subject;
        this.status = "open";
        this.createdAt = new Date();
        this.lastMessageAt = new Date();
        this.hasUnreadMessages = false;
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

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Date lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }

    public boolean hasUnreadMessages() { return hasUnreadMessages; }
    public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }

    public String getFormattedTime() {
        if (lastMessageAt == null) return "";
        long diff = System.currentTimeMillis() - lastMessageAt.getTime();
        long hours = diff / (1000 * 60 * 60);
        if (hours < 24) {
            return hours + "h ago";
        } else {
            long days = hours / 24;
            return days + "d ago";
        }
    }
}
