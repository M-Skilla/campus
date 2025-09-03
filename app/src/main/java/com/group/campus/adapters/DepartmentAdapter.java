package com.group.campus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.group.campus.R;
import com.group.campus.models.Department;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder> {

    private final List<Department> departments;
    private final OnDepartmentClickListener listener;

    public interface OnDepartmentClickListener {
        void onDepartmentClick(Department department);
    }

    public DepartmentAdapter(List<Department> departments, OnDepartmentClickListener listener) {
        this.departments = departments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DepartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_department, parent, false);
        return new DepartmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentViewHolder holder, int position) {
        Department department = departments.get(position);
        holder.bind(department, listener);
    }

    @Override
    public int getItemCount() {
        return departments.size();
    }

    static class DepartmentViewHolder extends RecyclerView.ViewHolder {

        private final TextView departmentName;
        private final TextView departmentDescription;

        public DepartmentViewHolder(@NonNull View itemView) {
            super(itemView);
            departmentName = itemView.findViewById(R.id.tv_department_name);
            departmentDescription = itemView.findViewById(R.id.tv_department_description);
        }

        public void bind(Department department, OnDepartmentClickListener listener) {
            departmentName.setText(department.getName());
            departmentDescription.setText(department.getDescription());
            itemView.setOnClickListener(v -> listener.onDepartmentClick(department));
        }
    }
}
