package com.group.campus.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group.campus.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing user roles and staff access
 */
public class UserRoleService {

    private static final String TAG = "UserRoleService";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface RoleCheckListener {
        void onRoleChecked(boolean isStaff, String department, UserRole userRole);
        void onError(Exception error);
    }

    public interface AdminOperationCallback {
        void onSuccess(String message);
        void onError(Exception error);
    }

    public interface UserCallback {
        void onUserLoaded(User user);
        void onError(String error);
    }

    public static class UserRole {
        public String userId;
        public String name;
        public String registrationNumber;
        public String role;
        public String department;
        public boolean isActive;

        public UserRole() {}

        public UserRole(String userId, String name, String registrationNumber, String role, String department, boolean isActive) {
            this.userId = userId;
            this.name = name;
            this.registrationNumber = registrationNumber;
            this.role = role;
            this.department = department;
            this.isActive = isActive;
        }
    }

    public UserRoleService() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Get current user and check their role
     */
    public void getCurrentUser(UserCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("No user logged in");
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Getting user data for: " + userId);

        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(userId);
                            callback.onUserLoaded(user);
                        } else {
                            callback.onError("Failed to parse user data");
                        }
                    } else {
                        callback.onError("User document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user document", e);
                    callback.onError("Error loading user: " + e.getMessage());
                });
    }

    /**
     * Check current user's role with detailed information
     */
    public void checkCurrentUserRole(RoleCheckListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user found");
            listener.onRoleChecked(false, null, null);
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Checking user role for userId: " + userId);

        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            // Get user data
                            String fullName = documentSnapshot.getString("fullName");
                            String regNo = documentSnapshot.getString("regNo");

                            // Check for staff role in roles array
                            boolean isStaff = false;
                            String department = "General";

                            List<String> rolesArray = (List<String>) documentSnapshot.get("roles");
                            if (rolesArray != null) {
                                isStaff = rolesArray.contains("staff");
                                Log.d(TAG, "Roles array: " + rolesArray + ", isStaff: " + isStaff);
                            }

                            // If staff, try to get department info
                            if (isStaff) {
                                Object collegeObj = documentSnapshot.get("college");
                                if (collegeObj instanceof Map) {
                                    Map<String, Object> college = (Map<String, Object>) collegeObj;
                                    String collegeName = (String) college.get("name");
                                    if (collegeName != null && !collegeName.isEmpty()) {
                                        department = collegeName;
                                    }
                                }
                            }

                            // Create UserRole object
                            UserRole userRole = new UserRole(
                                userId,
                                fullName != null ? fullName : "Unknown User",
                                regNo != null ? regNo : "Unknown",
                                isStaff ? "staff" : "student",
                                department,
                                true
                            );

                            listener.onRoleChecked(isStaff, department, userRole);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user data", e);
                            listener.onError(e);
                        }
                    } else {
                        listener.onError(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "Error checking user role", error);
                    listener.onError(error);
                });
    }

    /**
     * Check if user is staff
     */
    public boolean isStaff(User user) {
        if (user == null) return false;

        // Check if user has staff role
        if (user.getRoles() != null) {
            return checkStaffRole(user);
        }

        return false;
    }

    /**
     * Helper method to check staff role
     */
    private boolean checkStaffRole(User user) {
        try {
            Object roles = user.getRoles();
            if (roles != null) {
                return roles.toString().contains("staff");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking staff role", e);
        }

        return false;
    }

    /**
     * Add staff role to user
     */
    public void addStaffRole(String userId, String department) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .update("roles", FieldValue.arrayUnion("staff"))
                .addOnSuccessListener(aVoid ->
                    Log.d(TAG, "Staff role added successfully"))
                .addOnFailureListener(e ->
                    Log.e(TAG, "Error adding staff role", e));
    }

    /**
     * Remove staff role from user
     */
    public void removeStaffRole(String userId) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .update("roles", FieldValue.arrayRemove("staff"))
                .addOnSuccessListener(aVoid ->
                    Log.d(TAG, "Staff role removed successfully"))
                .addOnFailureListener(e ->
                    Log.e(TAG, "Error removing staff role", e));
    }

    /**
     * ADMIN FUNCTION: Grant staff access to a user by registration number
     */
    public void grantStaffAccess(String registrationNumber, String department, String userName, AdminOperationCallback callback) {
        Log.d(TAG, "Granting staff access to: " + registrationNumber + " for department: " + department);

        db.collection(USERS_COLLECTION)
            .whereEqualTo("regNo", registrationNumber)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                    String userId = userDoc.getId();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("staffDepartment", department);
                    updates.put("isActive", true);

                    if (userName != null && !userName.isEmpty()) {
                        updates.put("fullName", userName);
                    }

                    // Update user document with staff department
                    db.collection(USERS_COLLECTION)
                        .document(userId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            // Add staff role to roles array
                            addStaffRole(userId, department);
                            callback.onSuccess("Staff access granted successfully to " + registrationNumber + " for " + department + " department");
                        })
                        .addOnFailureListener(callback::onError);
                } else {
                    callback.onError(new Exception("User not found with registration number: " + registrationNumber));
                }
            })
            .addOnFailureListener(callback::onError);
    }

    /**
     * ADMIN FUNCTION: Revoke staff access from a user by registration number
     */
    public void revokeStaffAccess(String registrationNumber, AdminOperationCallback callback) {
        db.collection(USERS_COLLECTION)
            .whereEqualTo("regNo", registrationNumber)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                    String userId = userDoc.getId();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("staffDepartment", FieldValue.delete());

                    db.collection(USERS_COLLECTION)
                        .document(userId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            // Remove staff role from roles array
                            removeStaffRole(userId);
                            callback.onSuccess("Staff access revoked successfully from " + registrationNumber);
                        })
                        .addOnFailureListener(callback::onError);
                } else {
                    callback.onError(new Exception("User not found with registration number: " + registrationNumber));
                }
            })
            .addOnFailureListener(callback::onError);
    }
}
