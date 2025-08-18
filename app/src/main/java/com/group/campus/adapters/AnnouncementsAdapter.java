package com.group.campus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;
import com.group.campus.models.Announcement;
import com.group.campus.utils.OnItemClickListener;
import com.group.campus.utils.DateUtils;

import java.util.List;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView departmentText, dateText, titleText, contentText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            departmentText = itemView.findViewById(R.id.textView_announcement_department);
            dateText = itemView.findViewById(R.id.textView_announcement_date);
            titleText = itemView.findViewById(R.id.textView_announcement_title);
            contentText = itemView.findViewById(R.id.textView_announcement_content);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(v);
                } else { return; }
            });

        }
    }

    private List<Announcement> announcements;

    private OnItemClickListener listener;

    public AnnouncementsAdapter(List<Announcement> announcements, OnItemClickListener listener) {
        this.announcements = announcements;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_announcement, parent, false));
    }

    public void updateList(List<Announcement> newList) {
        announcements.clear();
        announcements.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.departmentText.setText(announcements.get(position).getDepartment());
        holder.contentText.setText(announcements.get(position).getBody());
        holder.titleText.setText(announcements.get(position).getTitle());
        holder.dateText.setText(DateUtils.getTimeAgo(announcements.get(position).getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }
}
