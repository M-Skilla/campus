package com.group.campus.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private final List<String> dates;
    private final int currentDay;

    public DateAdapter(List<String> dates, int currentDay) {
        this.dates = dates;
        this.currentDay = currentDay;
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = (TextView) itemView;
        }
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView view = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String date = dates.get(position);
        holder.dateText.setText(date);

        if (date.isEmpty()) {
            holder.dateText.setVisibility(View.INVISIBLE);
        } else {
            holder.dateText.setVisibility(View.VISIBLE);

            // Highlight current day (like Aug 17 in the screenshot)
            try {
                int dayNum = Integer.parseInt(date);
                if (dayNum == currentDay) {
                    holder.dateText.setBackgroundColor(Color.BLUE);
                    holder.dateText.setTextColor(Color.WHITE);
                } else {
                    holder.dateText.setBackgroundColor(Color.TRANSPARENT);
                    holder.dateText.setTextColor(Color.WHITE);
                }
            } catch (NumberFormatException e) {
                holder.dateText.setBackgroundColor(Color.TRANSPARENT);
                holder.dateText.setTextColor(Color.WHITE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }
}
