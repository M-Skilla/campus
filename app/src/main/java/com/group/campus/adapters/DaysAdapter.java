package com.group.campus.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DayViewHolder> {

    private final List<String> dayNumbers;

    public DaysAdapter(YearMonth month) {
        dayNumbers = new ArrayList<>();
        LocalDate firstDay = month.atDay(1);
        int startOffset = firstDay.getDayOfWeek().getValue() % 7; // Sunday = 0

        // Empty cells for offset
        for (int i = 0; i < startOffset; i++) dayNumbers.add("");

        // Add day numbers
        for (int day = 1; day <= month.lengthOfMonth(); day++) dayNumbers.add(String.valueOf(day));

        // Fill remaining cells to make full 42 (6x7)
        while (dayNumbers.size() < 42) dayNumbers.add("");
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.dayText.setText(dayNumbers.get(position));
        holder.dayText.setGravity(Gravity.CENTER);
    }

    @Override
    public int getItemCount() { return dayNumbers.size(); }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
        }
    }
}
