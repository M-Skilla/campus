package com.group.campus.models;

import java.util.Date;

public class User {
    // New canonical fields
    private String id, regNo, fullName, profilePicUrl;
    private College college;
    private Programme programme;
    private Role roles;
    private Date startDate, endDate;

    // Legacy/compat fields (kept only to avoid breaking existing code paths)
    // Prefer using new fields above. These are bridged via accessors.
    private String email; // optional legacy
    private String legacyDepartment; // optional legacy string department
    private String fcmToken; // optional legacy push token

    public User() {
    }

    public User(String id, String regNo, String fullName, String profilePicUrl,
                College college, Programme programme, Role roles,
                Date startDate, Date endDate) {
        this.id = id;
        this.regNo = regNo;
        this.fullName = fullName;
        this.profilePicUrl = profilePicUrl;
        this.college = college;
        this.programme = programme;
        this.roles = roles;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // New model accessors
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfilePicUrl() { return profilePicUrl; }
    public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }

    public College getCollege() { return college; }
    public void setCollege(College college) { this.college = college; }

    public Programme getProgramme() { return programme; }
    public void setProgramme(Programme programme) { this.programme = programme; }

    public Role getRoles() { return roles; }
    public void setRoles(Role roles) { this.roles = roles; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    // ---------- Backward-compatible bridge methods ----------
    // Keep older code compiling without immediate refactors.

    /** Legacy alias for id */
    public String getUserId() { return id; }
    public void setUserId(String userId) { this.id = userId; }

    /** Legacy alias for fullName */
    public String getName() { return fullName; }
    public void setName(String name) { this.fullName = name; }

    /** Optional legacy email (not part of the new canonical model) */
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * Legacy role as a String. Maps to roles.getName().
     */
    public String getRole() { return roles != null ? roles.getName() : null; }
    public void setRole(String role) {
        if (this.roles == null) {
            this.roles = new Role();
        }
        this.roles.setName(role);
    }

    /**
     * Legacy department string. If present, returned.
     * Otherwise, best-effort from programme/college names.
     */
    public String getDepartment() {
        if (legacyDepartment != null && !legacyDepartment.isEmpty()) return legacyDepartment;
        if (programme != null) {
            // Prefer programme name if available
            try {
                // Programme may have getters added separately
                // This try/catch guards older bytecode without getters.
                java.lang.reflect.Method m = programme.getClass().getMethod("getName");
                Object val = m.invoke(programme);
                if (val instanceof String && !((String) val).isEmpty()) return (String) val;
            } catch (Exception ignored) { }
        }
        if (college != null && college.getName() != null && !college.getName().isEmpty()) return college.getName();
        return null;
    }
    public void setDepartment(String department) {
        this.legacyDepartment = department;
        // Also reflect into college placeholder if nothing set
        if (this.college == null && department != null) {
            this.college = new College(null, department, null);
        }
    }

    /** Optional legacy FCM token accessors. */
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    @Override
    public String toString() {
        String dept = getDepartment();
        return (fullName != null ? fullName : "User") + (dept != null ? " (" + dept + ")" : "");
    }
}
