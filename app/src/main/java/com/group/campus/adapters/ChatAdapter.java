package com.group.campus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;
import com.group.campus.models.Suggestion;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_OUTGOING = 1;
    private static final int TYPE_INCOMING = 2;

    private final List<Suggestion> items = new ArrayList<>();
    private final String currentUserId;
    private final OnMessageClickListener clickListener;

    public interface OnMessageClickListener {
        void onMessageClick(Suggestion suggestion);
        void onMessageLongClick(Suggestion suggestion);
    }

    public ChatAdapter(String currentUserId, OnMessageClickListener clickListener) {
        this.currentUserId = currentUserId;
        this.clickListener = clickListener;
    }

    public void setItems(List<Suggestion> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void addItem(Suggestion s) {
        items.add(s);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        Suggestion s = items.get(position);
        return s.isOutgoing() ? TYPE_OUTGOING : TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_OUTGOING) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion_outgoing, parent, false);
            return new OutgoingVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion_incoming, parent, false);
            return new IncomingVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Suggestion s = items.get(position);
        if (holder instanceof OutgoingVH) {
            ((OutgoingVH) holder).bind(s, clickListener);
        } else if (holder instanceof IncomingVH) {
            ((IncomingVH) holder).bind(s, clickListener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OutgoingVH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp;
        OutgoingVH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
        void bind(Suggestion s, OnMessageClickListener click) {
            tvMessage.setText(s.getText());
            tvTimestamp.setText(s.getFormattedTime());
            itemView.setOnClickListener(v -> { if (click != null) click.onMessageClick(s); });
            itemView.setOnLongClickListener(v -> { if (click != null) click.onMessageLongClick(s); return true; });
        }
    }

    static class IncomingVH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp, tvAvatar;
        IncomingVH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
        }
        void bind(Suggestion s, OnMessageClickListener click) {
            tvMessage.setText(s.getText());
            tvTimestamp.setText(s.getFormattedTime());
            if (tvAvatar != null) {
                tvAvatar.setText(s.getAvatarInitials());
            }
            itemView.setOnClickListener(v -> { if (click != null) click.onMessageClick(s); });
            itemView.setOnLongClickListener(v -> { if (click != null) click.onMessageLongClick(s); return true; });
        }
    }
}
