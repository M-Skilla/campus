package com.group.campus.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.OnBackPressedCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.group.campus.R;

public class ChangePasswordFragment extends Fragment {

    private MaterialButton btnBack;
    private MaterialButton btnResetPassword;
    private TextInputEditText etOldPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private TextView tvPasswordMatch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        btnResetPassword = view.findViewById(R.id.btn_reset_password);
        etOldPassword = view.findViewById(R.id.et_old_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        tvPasswordMatch = view.findViewById(R.id.tv_password_match);

        btnBack.setOnClickListener(v -> handleBackNavigation());

        setupPasswordValidation();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        handleBackNavigation();
                    }
                }
        );
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
        etOldPassword.addTextChangedListener(passwordWatcher);
    }

    private void validatePasswords() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isValid = !oldPassword.isEmpty() &&
                !newPassword.isEmpty() &&
                !confirmPassword.isEmpty() &&
                newPassword.length() >= 6 &&
                newPassword.equals(confirmPassword);

        if (!confirmPassword.isEmpty() && newPassword.equals(confirmPassword)) {
            tvPasswordMatch.setVisibility(View.VISIBLE);
        } else {
            tvPasswordMatch.setVisibility(View.GONE);
        }

        btnResetPassword.setEnabled(isValid);
    }

    private void handleBackNavigation() {
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
            requireActivity().findViewById(R.id.settingsActivity).setVisibility(View.VISIBLE);
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            requireActivity().finish();
        }
    }
}