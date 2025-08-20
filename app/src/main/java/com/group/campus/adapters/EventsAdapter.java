package com.group.campus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<String> events;

    public EventsAdapter(List<String> events) {
        this.events = events;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle;
        TextView eventDate;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        String event = events.get(position);
        holder.eventTitle.setText(event);
        holder.eventDate.setText("Aug 20, 2025"); // Placeholder date
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
