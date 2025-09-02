package com.group.campus.service;

import com.google.firebase.firestore.FirebaseFirestore;
import com.group.campus.models.Department;

public class DepartmentInitService {
    private static final String COLLECTION_DEPARTMENTS = "departments";
    private final FirebaseFirestore db;

    public interface InitCallback {
        void onSuccess();
        void onError(Exception error);
    }

    public DepartmentInitService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void initializeDepartments(InitCallback callback) {
        // Check if departments already exist
        db.collection(COLLECTION_DEPARTMENTS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Create default departments
                        createDefaultDepartments(callback);
                    } else {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    private void createDefaultDepartments(InitCallback callback) {
        // Create 3 default departments
        Department[] departments = {
                new Department("", "Academic Affairs", "Handle academic related suggestions and concerns", true),
                new Department("", "Student Services", "Manage student welfare and support services", true),
                new Department("", "Administration", "General administrative matters and policies", true)
        };

        int[] completed = {0};
        int total = departments.length;

        for (Department department : departments) {
            db.collection(COLLECTION_DEPARTMENTS)
                    .add(department)
                    .addOnSuccessListener(documentReference -> {
                        completed[0]++;
                        if (completed[0] == total) {
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(callback::onError);
        }
    }
}
