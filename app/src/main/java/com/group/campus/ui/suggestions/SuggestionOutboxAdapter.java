package com.group.campus.ui.suggestions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;
import com.group.campus.model.SuggestionConversation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SuggestionOutboxAdapter extends RecyclerView.Adapter<SuggestionOutboxAdapter.OutboxViewHolder> {

    private List<SuggestionConversation> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(SuggestionConversation conversation);
    }

    public SuggestionOutboxAdapter(List<SuggestionConversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OutboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion_outbox, parent, false);
        return new OutboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutboxViewHolder holder, int position) {
        SuggestionConversation conversation = conversations.get(position);
        holder.bind(conversation, listener);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class OutboxViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSubject, tvDepartment, tvStatus, tvLastMessageTime, tvDeliveryStatus;
        private ImageView ivDepartmentIcon, ivStatusIcon;
        private View statusIndicator;

        public OutboxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tv_subject);
            tvDepartment = itemView.findViewById(R.id.tv_department);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvLastMessageTime = itemView.findViewById(R.id.tv_last_message_time);
            tvDeliveryStatus = itemView.findViewById(R.id.tv_delivery_status);
            ivDepartmentIcon = itemView.findViewById(R.id.iv_department_icon);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
        }

        public void bind(SuggestionConversation conversation, OnConversationClickListener listener) {
            tvSubject.setText(conversation.getSubject());
            tvDepartment.setText(conversation.getDepartment() + " Department");

            // Format time
            if (conversation.getLastMessageAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                tvLastMessageTime.setText(sdf.format(conversation.getLastMessageAt().toDate()));
            }

            // Set department-specific icon and colors
            setupDepartmentUI(conversation.getDepartment());

            // Set status and delivery info
            setupStatusUI(conversation);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });
        }

        private void setupDepartmentUI(String department) {
            int iconRes, bgColor, iconColor;
            switch (department) {
                case "Health":
                    iconRes = R.drawable.ic_health;
                    bgColor = R.color.health_bg;
                    iconColor = R.color.health_icon;
                    break;
                case "Facilities":
                    iconRes = R.drawable.ic_facilities;
                    bgColor = R.color.facilities_bg;
                    iconColor = R.color.facilities_icon;
                    break;
                case "Library":
                    iconRes = R.drawable.ic_library;
                    bgColor = R.color.library_bg;
                    iconColor = R.color.library_icon;
                    break;
                default:
                    iconRes = R.drawable.ic_suggestion;
                    bgColor = R.color.md_theme_primary;
                    iconColor = R.color.md_theme_onPrimary;
            }

            ivDepartmentIcon.setImageResource(iconRes);
            ivDepartmentIcon.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(bgColor, null));
            ivDepartmentIcon.setImageTintList(itemView.getContext().getResources().getColorStateList(iconColor, null));
        }

        private void setupStatusUI(SuggestionConversation conversation) {
            String status = conversation.getStatus();
            boolean hasUnreadMessages = conversation.isHasUnreadMessages();

            if ("resolved".equals(status)) {
                tvStatus.setText("RESOLVED");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.status_resolved, null));
                statusIndicator.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.status_resolved, null));
                ivStatusIcon.setImageResource(R.drawable.ic_check);
                ivStatusIcon.setImageTintList(itemView.getContext().getResources().getColorStateList(R.color.status_resolved, null));
                tvDeliveryStatus.setText("Conversation resolved");
            } else if (hasUnreadMessages) {
                tvStatus.setText("NEW REPLY");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.status_new_reply, null));
                statusIndicator.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.status_new_reply, null));
                ivStatusIcon.setImageResource(R.drawable.ic_mail);
                ivStatusIcon.setImageTintList(itemView.getContext().getResources().getColorStateList(R.color.status_new_reply, null));
                tvDeliveryStatus.setText("New reply from " + conversation.getDepartment());
            } else {
                tvStatus.setText("DELIVERED");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.status_delivered, null));
                statusIndicator.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.status_delivered, null));
                ivStatusIcon.setImageResource(R.drawable.ic_done);
                ivStatusIcon.setImageTintList(itemView.getContext().getResources().getColorStateList(R.color.status_delivered, null));
                tvDeliveryStatus.setText("Delivered to " + conversation.getDepartment());
            }
        }
    }
}
