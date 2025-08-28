package com.group.campus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;
import com.group.campus.models.Event;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<Event> events;
    private OnEventClickListener onEventClickListener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventsAdapter(List<Event> events) {
        this.events = events;
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.onEventClickListener = listener;
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle;
        TextView eventDate;
        TextView eventTime;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
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
        Event event = events.get(position);
        holder.eventTitle.setText(event.getTitle());
        holder.eventDate.setText(event.getFormattedDate());

        if (holder.eventTime != null) {
            holder.eventTime.setText(event.getFormattedTime());
        }

        // Set click listener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
