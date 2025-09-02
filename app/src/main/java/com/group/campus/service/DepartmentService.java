package com.group.campus.service;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.group.campus.models.Department;

import java.util.ArrayList;
import java.util.List;

public class DepartmentService {
    private static final String COLLECTION_DEPARTMENTS = "departments";
    private final FirebaseFirestore db;

    public interface DepartmentCallback {
        void onSuccess(List<Department> departments);
        void onError(Exception error);
    }

    public DepartmentService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getAllDepartments(DepartmentCallback callback) {
        db.collection(COLLECTION_DEPARTMENTS)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Department> departments = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Department department = document.toObject(Department.class);
                        department.setId(document.getId());
                        departments.add(department);
                    }
                    callback.onSuccess(departments);
                })
                .addOnFailureListener(callback::onError);
    }
}
