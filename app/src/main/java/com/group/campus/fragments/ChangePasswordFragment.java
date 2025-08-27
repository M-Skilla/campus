package com.group.campus.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group.campus.R;

public class ChangePasswordFragment extends Fragment {

    private TextInputEditText etOldPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnBack, btnResetPassword;
    private TextView tvPasswordMatch;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        initializeViews(view);
        setupClickListeners();
        setupPasswordValidation();

        return view;
    }

    private void initializeViews(View view) {
        etOldPassword = view.findViewById(R.id.et_old_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnBack = view.findViewById(R.id.btn_back);
        btnResetPassword = view.findViewById(R.id.btn_reset_password);
        tvPasswordMatch = view.findViewById(R.id.tv_password_match);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnResetPassword.setOnClickListener(v -> changePassword());
    }

    private void setupPasswordValidation() {
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswords();
            }
        };

        etNewPassword.addTextChangedListener(passwordWatcher);
        etConfirmPassword.addTextChangedListener(passwordWatcher);
    }

    private void validatePasswords() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(confirmPassword)) {
            if (newPassword.equals(confirmPassword)) {
                tvPasswordMatch.setVisibility(View.VISIBLE);
                tvPasswordMatch.setText("It's a match.");
                tvPasswordMatch.setTextColor(getResources().getColor(R.color.md_theme_primary));
                btnResetPassword.setEnabled(true);
            } else {
                tvPasswordMatch.setVisibility(View.VISIBLE);
                tvPasswordMatch.setText("Passwords don't match.");
                tvPasswordMatch.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                btnResetPassword.setEnabled(false);
            }
        } else {
            tvPasswordMatch.setVisibility(View.GONE);
            btnResetPassword.setEnabled(false);
        }
    }

    private void changePassword() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate input fields
        if (!validateInput(oldPassword, newPassword, confirmPassword)) {
            return;
        }

        if (currentUser != null && currentUser.getEmail() != null) {
            showLoading(true);

            // Re-authenticate user with current password
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);

            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(reauthTask -> {
                        if (reauthTask.isSuccessful()) {
                            updatePassword(newPassword);
                        } else {
                            showLoading(false);
                            String error = reauthTask.getException() != null ?
                                    reauthTask.getException().getMessage() : "Current password is incorrect";
                            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword(String newPassword) {
        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(updateTask -> {
                    showLoading(false);

                    if (updateTask.isSuccessful()) {
                        // Sign out first
                        mAuth.signOut();

                        Toast.makeText(getContext(), "Password changed successfully. Redirecting to login...", Toast.LENGTH_SHORT).show();
                        clearFields();

                        // Immediate redirect with proper cleanup
                        redirectToLoginImmediate();
                    } else {
                        String error = updateTask.getException() != null ?
                                updateTask.getException().getMessage() : "Failed to change password";
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redirectToLoginImmediate() {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (getContext() != null) {
                // Restart the entire app
                Intent restartIntent = getContext().getPackageManager()
                        .getLaunchIntentForPackage(getContext().getPackageName());

                if (restartIntent != null) {
                    restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(restartIntent);
                }

                // Force close current activity and exit app process
                if (getActivity() != null) {
                    getActivity().finishAffinity();
                }

                // Exit app process to ensure clean restart
                System.exit(0);
            }
        }, 2000);
    }

//    private void redirectToLogin() {
//        if (getActivity() != null) {
//            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
//                Intent intent = new Intent(getActivity(), MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//
//                if (getActivity() != null) {
//                    getActivity().finish();
//                }
//            }, 1500);
//        }
//    }

    private boolean validateInput(String oldPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(oldPassword)) {
            etOldPassword.setError("Current password is required");
            etOldPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (oldPassword.equals(newPassword)) {
            etNewPassword.setError("New password must be different from current password");
            etNewPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnResetPassword.setText("Changing Password...");
            btnResetPassword.setEnabled(false);
            btnBack.setEnabled(false);
        } else {
            btnResetPassword.setText("Change Password");
            btnResetPassword.setEnabled(true);
            btnBack.setEnabled(true);
        }
    }

    private void clearFields() {
        etOldPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
        tvPasswordMatch.setVisibility(View.GONE);
        btnResetPassword.setEnabled(false);
    }
}