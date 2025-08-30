package com.group.campus.ui.suggestions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;
import com.group.campus.model.SuggestionConversation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SuggestionConversationAdapter extends RecyclerView.Adapter<SuggestionConversationAdapter.ConversationViewHolder> {

    private final List<SuggestionConversation> conversations;
    private final OnConversationClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public interface OnConversationClickListener {
        void onConversationClick(SuggestionConversation conversation);
    }

    public SuggestionConversationAdapter(List<SuggestionConversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_suggestion_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        SuggestionConversation conversation = conversations.get(position);
        holder.bind(conversation, listener);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentName;
        private final TextView tvSubject;
        private final TextView tvLastMessage;
        private final TextView tvLastMessageTime;
        private final TextView tvStatus;
        private final View statusIndicator;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvSubject = itemView.findViewById(R.id.tv_subject);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvLastMessageTime = itemView.findViewById(R.id.tv_last_message_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
        }

        public void bind(SuggestionConversation conversation, OnConversationClickListener listener) {
            tvStudentName.setText(conversation.getStudentName());
            tvSubject.setText(conversation.getSubject());

            String lastMessage = conversation.getLastMessageText();
            if (lastMessage == null || lastMessage.isEmpty()) {
                tvLastMessage.setText("No messages yet");
            } else {
                tvLastMessage.setText(lastMessage);
            }

            if (conversation.getLastMessageAt() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                tvLastMessageTime.setText(dateFormat.format(conversation.getLastMessageAt().toDate()));
            }

            // Set status
            tvStatus.setText(conversation.getStatus().toUpperCase());

            // Set status indicator color
            if ("open".equals(conversation.getStatus())) {
                statusIndicator.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_green_light));
            } else {
                statusIndicator.setBackgroundColor(itemView.getContext().getColor(android.R.color.darker_gray));
            }

            // Highlight unread conversations
            if (conversation.isHasUnreadMessages()) {
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.md_theme_surfaceContainerHighest));
            } else {
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.md_theme_surface));
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });
        }
    }
}
