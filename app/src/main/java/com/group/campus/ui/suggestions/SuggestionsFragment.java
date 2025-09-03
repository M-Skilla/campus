package com.group.campus.ui.suggestions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import com.group.campus.R;
import com.group.campus.adapters.DepartmentAdapter;
import com.group.campus.models.Department;
import com.group.campus.service.DepartmentService;
import com.group.campus.service.DepartmentInitService;
import com.group.campus.service.UserRoleService;

import java.util.ArrayList;
import java.util.List;

public class SuggestionsFragment extends Fragment {

    private MaterialButton btnOutbox, btnInbox, btnStaffView;
    private UserRoleService userRoleService;
    private RecyclerView rvDepartments;
    private DepartmentAdapter departmentAdapter;
    private DepartmentService departmentService;
    private DepartmentInitService departmentInitService;
    private List<Department> departments = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRoleService = new UserRoleService();
        departmentService = new DepartmentService();
        departmentInitService = new DepartmentInitService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggestions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView(view);
        applyBottomNavPadding();
        // Determine staff visibility up-front
        evaluateStaffButtonVisibility();
        loadDepartments();
        setupClickListeners();
    }

    private void initViews(View view) {
        btnOutbox = view.findViewById(R.id.btn_outbox);
        btnInbox = view.findViewById(R.id.btn_inbox);
        btnStaffView = view.findViewById(R.id.btn_staff_view);
        // Hide staff button by default until verified
        if (btnStaffView != null) {
            btnStaffView.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView(View view) {
        rvDepartments = view.findViewById(R.id.rv_departments);
        departmentAdapter = new DepartmentAdapter(departments, department -> {
            // Handle department click - open suggestion input
            openWriteSuggestion(department.getName());
        });
        rvDepartments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDepartments.setAdapter(departmentAdapter);
    }

    private void applyBottomNavPadding() {
        if (getActivity() == null) return;
        View nav = getActivity().findViewById(R.id.customBottomNav);
        if (nav == null || rvDepartments == null) return;

        ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                int h = nav.getHeight();
                int current = rvDepartments.getPaddingBottom();
                if (h != current) {
                    rvDepartments.setPadding(
                        rvDepartments.getPaddingLeft(),
                        rvDepartments.getPaddingTop(),
                        rvDepartments.getPaddingRight(),
                        h
                    );
                    rvDepartments.setClipToPadding(false);
                }
            }
        };
        nav.getViewTreeObserver().addOnGlobalLayoutListener(listener);
        rvDepartments.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    private void loadDepartments() {
        departmentService.getAllDepartments(new DepartmentService.DepartmentCallback() {
            @Override
            public void onSuccess(List<Department> fetchedDepartments) {
                departments.clear();
                departments.addAll(fetchedDepartments);
                departmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(getContext(), "Failed to load departments: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnOutbox.setOnClickListener(v -> openOutbox());
        btnInbox.setOnClickListener(v -> openInbox());
        btnStaffView.setOnClickListener(v -> navigateToStaffIfAllowed());
    }

    private void openWriteSuggestion(String department) {
        WriteSuggestionFragment fragment = WriteSuggestionFragment.newInstance(department, null, false);
        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void openOutbox() {
        SuggestionOutboxFragment fragment = new SuggestionOutboxFragment();
        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void openInbox() {
        SuggestionInboxFragment fragment = new SuggestionInboxFragment();
        getParentFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void navigateToStaffIfAllowed() {
        btnStaffView.setEnabled(false);
        userRoleService.checkCurrentUserRole(new UserRoleService.RoleCheckListener() {
            @Override
            public void onRoleChecked(boolean isStaff, String department, UserRoleService.UserRole userRole) {
                btnStaffView.setEnabled(true);
                if (isStaff) {
                    SuggestionStaffDashboardFragment staffFragment = new SuggestionStaffDashboardFragment();
                    getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, staffFragment)
                        .addToBackStack(null)
                        .commit();
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "You don't have staff access", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception error) {
                btnStaffView.setEnabled(true);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error checking access: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void evaluateStaffButtonVisibility() {
        // Verify role and show staff button only for staff users
        userRoleService.checkCurrentUserRole(new UserRoleService.RoleCheckListener() {
            @Override
            public void onRoleChecked(boolean isStaff, String department, UserRoleService.UserRole userRole) {
                if (btnStaffView == null) return;
                if (isStaff) {
                    btnStaffView.setVisibility(View.VISIBLE);
                } else {
                    btnStaffView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception error) {
                if (btnStaffView != null) btnStaffView.setVisibility(View.GONE);
            }
        });
    }
}
