package com.group.campus.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;

import java.time.YearMonth;
import java.util.List;

public class YearAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {

    public interface OnMonthClickListener {
        void onMonthClick(YearMonth month);
    }

    private final List<YearMonth> months;
    private final OnMonthClickListener listener;

    public YearAdapter(List<YearMonth> months, OnMonthClickListener listener) {
        this.months = months;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MonthAdapter.MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MonthAdapter.MonthViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.calendar_day_layout, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MonthAdapter.MonthViewHolder holder, int position) {
        YearMonth month = months.get(position);
        holder.bind(month);

        holder.itemView.setOnClickListener(v -> listener.onMonthClick(month));
    }

    @Override
    public int getItemCount() {
        return months.size();
    }
}
