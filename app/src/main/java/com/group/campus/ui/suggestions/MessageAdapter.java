package com.group.campus.ui.suggestions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;
import com.group.campus.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_STUDENT = 0;
    private static final int VIEW_TYPE_STAFF = 1;

    private List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.isFromStudent() ? VIEW_TYPE_STUDENT : VIEW_TYPE_STAFF;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_STUDENT) {
            View view = inflater.inflate(R.layout.item_message_student, parent, false);
            return new StudentMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_staff, parent, false);
            return new StaffMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof StudentMessageViewHolder) {
            ((StudentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof StaffMessageViewHolder) {
            ((StaffMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder for student messages (right-aligned)
    static class StudentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvTimestamp;
        private ImageView ivAttachment;

        public StudentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            ivAttachment = itemView.findViewById(R.id.iv_attachment);
        }

        public void bind(Message message) {
            tvMessage.setText(message.getText());
            tvTimestamp.setText(message.getFormattedTime());

            if (message.hasAttachment()) {
                ivAttachment.setVisibility(View.VISIBLE);
                // TODO: Load attachment preview
            } else {
                ivAttachment.setVisibility(View.GONE);
            }
        }
    }

    // ViewHolder for staff messages (left-aligned)
    static class StaffMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSenderName;
        private TextView tvMessage;
        private TextView tvTimestamp;
        private ImageView ivAttachment;

        public StaffMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            ivAttachment = itemView.findViewById(R.id.iv_attachment);
        }

        public void bind(Message message) {
            tvSenderName.setText(message.getSenderName());
            tvMessage.setText(message.getText());
            tvTimestamp.setText(message.getFormattedTime());

            if (message.hasAttachment()) {
                ivAttachment.setVisibility(View.VISIBLE);
                // TODO: Load attachment preview
            } else {
                ivAttachment.setVisibility(View.GONE);
            }
        }
    }
}
