package com.group.campus.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.applandeo.materialcalendarview.CalendarView;
import com.group.campus.R;

import java.util.Calendar;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {

    private final int year;

    public MonthAdapter(int year) {
        this.year = year;
    }

    public static class MonthViewHolder extends RecyclerView.ViewHolder {
        CalendarView calendar;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            calendar = itemView.findViewById(R.id.monthCalendar);
        }
    }

    @NonNull
    @Override

    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_month, parent, false);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, position); // 0 = Jan, 11 = Dec
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        holder.calendar.setDate(calendar.getTime());

        // Hide month name (header) so only dates remain
        holder.calendar.setHeaderLabelColor(android.R.color.transparent);
        holder.calendar.setHeaderColor(android.R.color.transparent);

    }

    @Override
    public int getItemCount() {

        return 12; // 12 months

    }
}
