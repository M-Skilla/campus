package com.group.campus.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group.campus.R;
import com.group.campus.models.Announcement;
import com.group.campus.utils.AnnouncementDiffCallback;
import com.group.campus.utils.OnItemClickListener;
import com.group.campus.utils.DateUtils;
import com.group.campus.utils.HtmlRenderer;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.ViewHolder> {
    private static final String TAG = "AnnouncementsAdapter";
    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView departmentText, dateText, titleText, contentText;

        private ImageView image, avatar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.imageView_department_avatar);
            image = itemView.findViewById(R.id.imageView_announcement_image);
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
        List<Announcement> oldList = new ArrayList<>(this.announcements);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new AnnouncementDiffCallback(oldList, newList));
        announcements.clear();
        announcements.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.departmentText.setText(announcements.get(position).getDepartment());

        // Render HTML content properly with ordered list support
        String bodyContent = announcements.get(position).getBody();
        if (bodyContent != null && bodyContent.contains("<")) {
            // Content contains HTML, render it properly with ordered list support
            holder.contentText.setText(HtmlRenderer.fromHtml(bodyContent));
        } else {
            // Plain text content
            holder.contentText.setText(bodyContent);
        }

        holder.titleText.setText(announcements.get(position).getTitle());
        holder.dateText.setText(DateUtils.getTimeAgo(announcements.get(position).getCreatedAt()));


        String profilePic = null;
        if (announcements.get(position).getAuthor() != null) {
            profilePic = announcements.get(position).getAuthor().getProfilePicUrl();
        }

        if (profilePic != null && !profilePic.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(profilePic)
                    .placeholder(R.drawable.profile_image)
                    .error(R.drawable.profile_image)
                    .into(holder.avatar);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load((String) null)
                    .placeholder(R.drawable.profile_image)
                    .error(R.drawable.profile_image)
                    .into(holder.avatar);
        }
        List<String> imageUrls = announcements.get(position).getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String imageUrl = imageUrls.get(0);
            System.out.println(imageUrl);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_loading_24)
                    .error(R.drawable.udom_logo)
                    .into(holder.image);
        } else {
            // No image available, show placeholder

            holder.image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Glide.with(holder.itemView.getContext())
                    .load((String) null)
                    .placeholder(R.drawable.ic_image_loading_24)
                    .error(R.drawable.udom_logo)
                    .into(holder.image);
        }

    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }
}
