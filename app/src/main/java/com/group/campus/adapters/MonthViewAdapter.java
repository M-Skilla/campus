package com.group.campus.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;
import com.group.campus.managers.EventManager;


import java.util.Calendar;

public class MonthViewAdapter extends RecyclerView.Adapter<MonthViewAdapter.DayViewHolder> {

    private final int year;
    private final int month;
    private final String[] monthNames = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private final String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private EventManager eventManager;

    public MonthViewAdapter(int year, int month) {
        this.year = year;
        this.month = month;
        this.eventManager = EventManager.getInstance();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;
        View eventIndicator;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
            eventIndicator = itemView.findViewById(R.id.eventIndicator);
        }
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_cell, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        if (position < 7) {
            // Header row with day names
            holder.dayText.setText(dayNames[position]);

            holder.dayText.setTextSize(12f);
            if (holder.eventIndicator != null) {
                holder.eventIndicator.setVisibility(View.GONE);
            }
            return;
        }

        // Calculate calendar position
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int dayPosition = position - 7; // Subtract header row
        int day = dayPosition - firstDayOfWeek + 1;

        if (day > 0 && day <= daysInMonth) {
            holder.dayText.setText(String.valueOf(day));

            holder.dayText.setTextSize(16f);

            // Highlight current day (August 20, 2025)
            if (month == 7 && day == 20) { // August is month 7 (0-indexed)
                holder.dayText.setBackgroundColor(Color.BLUE);
            } else {
                holder.dayText.setBackgroundColor(Color.TRANSPARENT);
            }

            // Show event indicator if there are events on this day
            if (holder.eventIndicator != null) {
                if (eventManager.hasEventsOnDate(year, month, day)) {
                    holder.eventIndicator.setVisibility(View.VISIBLE);
                } else {
                    holder.eventIndicator.setVisibility(View.GONE);
                }
            }
        } else {
            holder.dayText.setText("");
            holder.dayText.setBackgroundColor(Color.TRANSPARENT);
            if (holder.eventIndicator != null) {
                holder.eventIndicator.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 49; // 7 header cells + 42 day cells (6 weeks)
    }

    public void refreshEvents() {
        notifyDataSetChanged();
    }
}
