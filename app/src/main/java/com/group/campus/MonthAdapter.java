package com.group.campus;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.YearMonth;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.DayViewHolder> {

    private final YearMonth month;

    public MonthAdapter(YearMonth month) {
        this.month = month;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_day_layout, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(month);
    }

    @Override
    public int getItemCount() {
        return 1; // Only one month per adapter
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        TextView monthTitle;
        GridLayout daysGrid;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            monthTitle = itemView.findViewById(R.id.monthTitle);
            daysGrid = itemView.findViewById(R.id.daysGrid);
        }

        void bind(YearMonth month) {
            monthTitle.setText(capitalize(month.getMonth().name()));

            daysGrid.removeAllViews();
            daysGrid.setColumnCount(7);

            // Add weekday headers
            String[] weekdays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (String day : weekdays) {
                TextView header = new TextView(itemView.getContext());
                header.setText(day);
                header.setGravity(Gravity.CENTER);
                header.setLayoutParams(new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f)
                ));
                daysGrid.addView(header);
            }

            LocalDate firstDay = month.atDay(1);
            int startOffset = (firstDay.getDayOfWeek().getValue() + 6) % 7; // Monday=0

            // Add empty placeholders
            for (int i = 0; i < startOffset; i++) {
                TextView empty = new TextView(itemView.getContext());
                empty.setText("");
                empty.setGravity(Gravity.CENTER);
                empty.setLayoutParams(new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f)
                ));
                daysGrid.addView(empty);
            }

            // Add days
            for (int day = 1; day <= month.lengthOfMonth(); day++) {
                TextView dayView = new TextView(itemView.getContext());
                dayView.setText(String.valueOf(day));
                dayView.setGravity(Gravity.CENTER);
                dayView.setLayoutParams(new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f)
                ));
                daysGrid.addView(dayView);
            }
        }

        private String capitalize(String s) {
            String lower = s.toLowerCase();
            return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        }
    }
}
