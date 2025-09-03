package com.group.campus.fragments;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group.campus.R;
import com.group.campus.SettingsActivity;
import com.group.campus.models.Programme;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;
import java.util.Objects;


public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private FirebaseFirestore db;

    private FirebaseAuth auth;

    private TextView nameInput;
    private TextView registrationInput;
    private TextView courseInput;
    private TextView userRoleTextView;

    private CircularProgressIndicator progressIndicator;

    private ImageView profileImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        db = FirebaseFirestore.getInstance();

        // Initialize views
        nameInput = view.findViewById(R.id.name_input);
        registrationInput = view.findViewById(R.id.registrationEditText);
        courseInput = view.findViewById(R.id.course_input);
        userRoleTextView = view.findViewById(R.id.user_id);
        profileImageView = view.findViewById(R.id.profile_image);

        progressIndicator = view.findViewById(R.id.progress_indicator);

        progressIndicator.setVisibility(View.VISIBLE);

        // Fetch user data from Firestore
        fetchUserDataFromFirestore();
        Button settingsIcon = view.findViewById(R.id.settings_icon);

        // Set click listener to open SettingsActivity
        settingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), SettingsActivity.class);
            startActivity(intent);
        });
        return view;
    }

    private void fetchUserDataFromFirestore() {

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "No authenticated user found");
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            progressIndicator.setVisibility(View.GONE);
            return;
        }
        String registrationNumber = Objects.requireNonNull(auth.getCurrentUser().getEmail()).split("@")[0].toUpperCase();
        if (registrationNumber == null) {
            Log.e(TAG, "User email is null");
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            progressIndicator.setVisibility(View.GONE);
            return;
        }

        Log.d(TAG, "fetchUserDataFromFirestore: Reg No: " + registrationNumber);
        // Query Firestore for user data using registration number
        db.collection("users")
                .whereEqualTo("regNo", registrationNumber)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    progressIndicator.setVisibility(View.GONE);

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String name = document.getString("fullName");
                        String regNumber = document.getString("regNo");
                        String imageUrl = document.getString("profilePicUrl");
                        Programme programme = document.get("programme", Programme.class);
                        String course = "N/A";
                        if (programme != null) {
                            course = String.format("%s (%s)", programme.getName(), programme.getAbbrv());
                        }

                        // Update UI with fetched data

                        updateUI(name, regNumber, imageUrl, course); // New: Pass imageUrl to updateUI

                        Log.d(TAG, "User data fetched successfully");
                    } else {
                        Log.d(TAG, "No user found with registration number: " + registrationNumber);
                        Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching user data", e);
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateUI(String name, String registrationNumber, String imageUrl, String course) {


        if (!isAdded() || getActivity() == null || isDetached()) {
            return;
        }


        if (name != null && !name.isEmpty()) {
            nameInput.setText(name);
        }

        if (registrationNumber != null && !registrationNumber.isEmpty()) {
            registrationInput.setText(registrationNumber);
        }

        if (course != null && !course.isEmpty()) {
            courseInput.setText(course);
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.profile_image) // Optional: Placeholder image
                    .error(R.drawable.profile_error) // Optional: Error image
                    .into(profileImageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.profile_image)
                    .placeholder(R.drawable.profile_image) // Optional: Placeholder image
                    .error(R.drawable.profile_error) // Optional: Error image
                    .into(profileImageView);
        }

    }

}
