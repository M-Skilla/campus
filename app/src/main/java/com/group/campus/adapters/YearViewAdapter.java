package com.group.campus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;
import com.group.campus.managers.EventManager;

import java.util.Calendar;

public class YearViewAdapter extends RecyclerView.Adapter<YearViewAdapter.MonthViewHolder> {

    private final int year;
    private final String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private OnMonthClickListener onMonthClickListener;
    private final EventManager eventManager;
    private final Calendar currentDate;

    public interface OnMonthClickListener {
        void onMonthClick(int month);
    }

    public YearViewAdapter(int year) {
        this.year = year;
        this.eventManager = EventManager.getInstance();
        this.currentDate = Calendar.getInstance();
    }

    public void setOnMonthClickListener(OnMonthClickListener listener) {
        this.onMonthClickListener = listener;
    }

    public static class MonthViewHolder extends RecyclerView.ViewHolder {
        TextView monthNameText;
        TextView[] dayViews = new TextView[42]; // 6 weeks * 7 days

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            monthNameText = itemView.findViewById(R.id.monthNameText);

            // Get all day TextViews
            dayViews[0] = itemView.findViewById(R.id.day1);
            dayViews[1] = itemView.findViewById(R.id.day2);
            dayViews[2] = itemView.findViewById(R.id.day3);
            dayViews[3] = itemView.findViewById(R.id.day4);
            dayViews[4] = itemView.findViewById(R.id.day5);
            dayViews[5] = itemView.findViewById(R.id.day6);
            dayViews[6] = itemView.findViewById(R.id.day7);
            dayViews[7] = itemView.findViewById(R.id.day8);
            dayViews[8] = itemView.findViewById(R.id.day9);
            dayViews[9] = itemView.findViewById(R.id.day10);
            dayViews[10] = itemView.findViewById(R.id.day11);
            dayViews[11] = itemView.findViewById(R.id.day12);
            dayViews[12] = itemView.findViewById(R.id.day13);
            dayViews[13] = itemView.findViewById(R.id.day14);
            dayViews[14] = itemView.findViewById(R.id.day15);
            dayViews[15] = itemView.findViewById(R.id.day16);
            dayViews[16] = itemView.findViewById(R.id.day17);
            dayViews[17] = itemView.findViewById(R.id.day18);
            dayViews[18] = itemView.findViewById(R.id.day19);
            dayViews[19] = itemView.findViewById(R.id.day20);
            dayViews[20] = itemView.findViewById(R.id.day21);
            dayViews[21] = itemView.findViewById(R.id.day22);
            dayViews[22] = itemView.findViewById(R.id.day23);
            dayViews[23] = itemView.findViewById(R.id.day24);
            dayViews[24] = itemView.findViewById(R.id.day25);
            dayViews[25] = itemView.findViewById(R.id.day26);
            dayViews[26] = itemView.findViewById(R.id.day27);
            dayViews[27] = itemView.findViewById(R.id.day28);
            dayViews[28] = itemView.findViewById(R.id.day29);
            dayViews[29] = itemView.findViewById(R.id.day30);
            dayViews[30] = itemView.findViewById(R.id.day31);
            dayViews[31] = itemView.findViewById(R.id.day32);
            dayViews[32] = itemView.findViewById(R.id.day33);
            dayViews[33] = itemView.findViewById(R.id.day34);
            dayViews[34] = itemView.findViewById(R.id.day35);
            dayViews[35] = itemView.findViewById(R.id.day36);
            dayViews[36] = itemView.findViewById(R.id.day37);
            dayViews[37] = itemView.findViewById(R.id.day38);
            dayViews[38] = itemView.findViewById(R.id.day39);
            dayViews[39] = itemView.findViewById(R.id.day40);
            dayViews[40] = itemView.findViewById(R.id.day41);
            dayViews[41] = itemView.findViewById(R.id.day42);
        }
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_year_view, parent, false);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        // Set month name
        holder.monthNameText.setText(monthNames[position]);

        // Add click listener to the entire month view
        holder.itemView.setOnClickListener(v -> {
            if (onMonthClickListener != null) {
                onMonthClickListener.onMonthClick(position);
            }
        });

        // Generate calendar for this month
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, position, 1);

        // Get first day of week for this month (0 = Sunday, 1 = Monday, etc.)
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // Get number of days in this month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Clear all day views first
        for (int i = 0; i < 42; i++) {
            holder.dayViews[i].setText("");
            holder.dayViews[i].setBackground(null);
            holder.dayViews[i].setVisibility(View.INVISIBLE);
        }

        // Fill in the days
        for (int day = 1; day <= daysInMonth; day++) {
            int positionInGrid = firstDayOfWeek + day - 1;
            if (positionInGrid < 42) {
                TextView dayView = holder.dayViews[positionInGrid];
                dayView.setText(String.valueOf(day));
                dayView.setVisibility(View.VISIBLE);

                // Check if this is the current date
                boolean isCurrentDate = (year == currentDate.get(Calendar.YEAR) &&
                        position == currentDate.get(Calendar.MONTH) &&
                        day == currentDate.get(Calendar.DAY_OF_MONTH));

                // Check if this day has events
                boolean hasEvents = eventManager.hasEventsOnDate(year, position, day);

                if (isCurrentDate) {
                    // Apply blue background for current date
                    dayView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_blue_light));
                    dayView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                } else if (hasEvents) {
                    // Apply grey background for days with events
                    dayView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
                    dayView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                } else {
                    // Reset background for regular days
                    dayView.setBackground(null);

                }

                // Clear any compound drawables (no dots needed)
                dayView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 12; // 12 months
    }

    /**
     * Refresh the adapter to update event indicators and current date highlighting
     */
    public void refreshEvents() {
        notifyDataSetChanged();
    }
}
