package com.group.campus.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing user roles and staff access
 * Uses registration number instead of email for user identification
 */
public class UserRoleService {

    private static final String TAG = "UserRoleService";
    private static final String USERS_COLLECTION = "users";
    private static final String DEPARTMENT_COLLECTION = "department";

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

    public static class UserRole {
        public String userId;
        public String name;
        public String registrationNumber;
        public String role; // primary role string for backward compat
        public String department; // staff department if staff
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
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Check if current logged-in user is staff and get their department
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
            .addOnSuccessListener(userDoc -> {
                if (userDoc.exists()) {
                    Log.d(TAG, "User document found: " + userDoc.getData());

                    // Get user basic info
                    String fullName = getStringSafe(userDoc, "fullName");
                    String regNo = getStringSafe(userDoc, "regNo");

                    // Check for staff role in roles array
                    boolean isStaff = false;
                    String department = null;

                    List<String> rolesArray = (List<String>) userDoc.get("roles");
                    if (rolesArray != null) {
                        isStaff = rolesArray.contains("staff");
                        Log.d(TAG, "Roles array: " + rolesArray + ", isStaff: " + isStaff);
                    }

                    // If user is staff, get their department
                    if (isStaff) {
                        // Check multiple possible field names for department
                        department = getStringSafe(userDoc, "staffDepartment");
                        if (department == null || department.isEmpty()) {
                            department = getStringSafe(userDoc, "department");
                        }

                        Log.d(TAG, "Staff user found with department: " + department);

                        // Verify department is valid (Health, Facilities, Library)
                        if (department != null && isValidDepartment(department)) {
                            UserRole userRole = new UserRole(userId, fullName, regNo, "staff", department, true);
                            listener.onRoleChecked(true, department, userRole);
                            return;
                        } else {
                            Log.w(TAG, "Invalid or missing department for staff user: " + department);
                        }
                    }

                    // User is not staff or has invalid department
                    UserRole userRole = new UserRole(userId, fullName, regNo, "student", null, true);
                    listener.onRoleChecked(false, null, userRole);
                } else {
                    Log.w(TAG, "User document does not exist for userId: " + userId);
                    listener.onRoleChecked(false, null, null);
                }
            })
            .addOnFailureListener(error -> {
                Log.e(TAG, "Error checking user role", error);
                listener.onError(error);
            });
    }

    /**
     * Check if department is valid
     */
    private boolean isValidDepartment(String department) {
        return department != null && (
            department.equalsIgnoreCase("Health") ||
            department.equalsIgnoreCase("Facilities") ||
            department.equalsIgnoreCase("Library")
        );
    }

    /**
     * ADMIN FUNCTION: Grant staff access to a user by registration number
     */
    public void grantStaffAccess(String registrationNumber, String department, String userName, AdminOperationCallback callback) {
        Log.d(TAG, "Granting staff access to: " + registrationNumber + " for department: " + department);

        // Validate department first
        if (!isValidDepartment(department)) {
            callback.onError(new Exception("Invalid department. Must be Health, Facilities, or Library"));
            return;
        }

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
                            db.collection(USERS_COLLECTION)
                                .document(userId)
                                .update("roles", FieldValue.arrayUnion("staff"))
                                .addOnSuccessListener(a2 -> {
                                    Log.d(TAG, "Successfully granted staff access to " + registrationNumber);
                                    callback.onSuccess("Staff access granted successfully to " + registrationNumber + " for " + department + " department");
                                })
                                .addOnFailureListener(callback::onError);
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
                            db.collection(USERS_COLLECTION)
                                .document(userId)
                                .update("roles", FieldValue.arrayRemove("staff"))
                                .addOnSuccessListener(a2 -> {
                                    callback.onSuccess("Staff access revoked successfully from " + registrationNumber);
                                })
                                .addOnFailureListener(callback::onError);
                        })
                        .addOnFailureListener(callback::onError);
                } else {
                    callback.onError(new Exception("User not found with registration number: " + registrationNumber));
                }
            })
            .addOnFailureListener(callback::onError);
    }

    /**
     * Helper method to safely get string from document
     */
    private String getStringSafe(DocumentSnapshot doc, String field) {
        Object value = doc.get(field);
        return value != null ? value.toString() : null;
    }

    public interface StaffListCallback {
        void onStaffListReceived(java.util.List<UserRole> staffList);
        void onError(Exception error);
    }
}
