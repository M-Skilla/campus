package com.group.campus.ui.suggestions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.group.campus.R;
import com.group.campus.model.Suggestion;

import java.util.Objects;

public class SuggestionAdapter extends ListAdapter<Suggestion, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_INCOMING = 0;
    private static final int VIEW_TYPE_OUTGOING = 1;

    private final OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionLongClick(Suggestion suggestion);
        void onAttachmentClick(Suggestion suggestion);
    }

    public SuggestionAdapter(OnSuggestionClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isOutgoing() ? VIEW_TYPE_OUTGOING : VIEW_TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        try {
            if (viewType == VIEW_TYPE_OUTGOING) {
                View view = inflater.inflate(R.layout.item_suggestion_outgoing, parent, false);
                return new OutgoingViewHolder(view);
            } else {
                View view = inflater.inflate(R.layout.item_suggestion_incoming, parent, false);
                return new IncomingViewHolder(view);
            }
        } catch (Throwable inflateError) {
            // Programmatic fallback bubble to avoid Premature end of file crash
            Context ctx = parent.getContext();
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(16, 16, 16, 16);
            TextView tv = new TextView(ctx);
            tv.setId(R.id.tvMessage);
            tv.setTextColor(ctx.getResources().getColor(R.color.textPrimary));
            tv.setTextSize(16);
            row.addView(tv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerView.ViewHolder(row) {};
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Suggestion suggestion = getItem(position);

        // Add slide-in animation for new items
        if (holder.itemView.getAnimation() == null) {
            holder.itemView.setAnimation(AnimationUtils.loadAnimation(
                holder.itemView.getContext(),
                suggestion.isOutgoing() ? R.anim.slide_in_right : R.anim.slide_in_left
            ));
        }

        if (holder instanceof OutgoingViewHolder) {
            ((OutgoingViewHolder) holder).bind(suggestion, listener);
        } else if (holder instanceof IncomingViewHolder) {
            ((IncomingViewHolder) holder).bind(suggestion, listener);
        }
    }

    private static void loadAttachment(ImageView iv, Suggestion s, Context ctx) {
        if (!s.hasAttachment()) {
            iv.setVisibility(View.GONE);
            return;
        }
        iv.setVisibility(View.VISIBLE);
        String type = s.getAttachmentType() == null ? "" : s.getAttachmentType();
        String url = s.getAttachmentUrl();
        // Use Glide for images and videos; fallback to icons for docs
        if (type.startsWith("image")) {
            Glide.with(ctx)
                .load(url)
                .apply(new RequestOptions().transform(new CenterCrop())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder))
                .into(iv);
        } else if (type.startsWith("video")) {
            Glide.with(ctx)
                .load(url)
                .frame(1_000_000) // extract a frame at ~1s
                .apply(new RequestOptions().transform(new CenterCrop())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder))
                .into(iv);
        } else if ("pdf".equalsIgnoreCase(type) || type.contains("pdf")) {
            iv.setImageResource(R.drawable.ic_picture_as_pdf);
        } else if (type.contains("word") || type.contains("msword") || type.contains("document") || type.contains("doc")) {
            iv.setImageResource(R.drawable.ic_document);
        } else {
            // Unknown/other file types
            iv.setImageResource(R.drawable.ic_attachment);
        }
    }

    // Incoming message ViewHolder
    static class IncomingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar;
        private final TextView tvMessage;
        private final TextView tvTimestamp;
        private final ImageView ivAttachment;

        public IncomingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivAttachment = itemView.findViewById(R.id.ivAttachment);
        }

        public void bind(Suggestion suggestion, OnSuggestionClickListener listener) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(suggestion.getText());
            tvTimestamp.setText(suggestion.getFormattedTime());
            tvAvatar.setText(suggestion.getAvatarInitials());

            if (suggestion.hasAttachment()) {
                loadAttachment(ivAttachment, suggestion, itemView.getContext());
                ivAttachment.setOnClickListener(v -> {
                    if (listener != null) listener.onAttachmentClick(suggestion);
                });
            } else {
                ivAttachment.setVisibility(View.GONE);
            }

            itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onSuggestionLongClick(suggestion);
                return true;
            });
        }
    }

    // Outgoing message ViewHolder
    static class OutgoingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final TextView tvTimestamp;
        private final ImageView ivStatus;
        private final ImageView ivAttachment;

        public OutgoingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            ivAttachment = itemView.findViewById(R.id.ivAttachment);
        }

        public void bind(Suggestion suggestion, OnSuggestionClickListener listener) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(suggestion.getText());
            tvTimestamp.setText(suggestion.getFormattedTime());
            updateStatusIcon(suggestion.getStatus());

            if (suggestion.hasAttachment()) {
                loadAttachment(ivAttachment, suggestion, itemView.getContext());
                ivAttachment.setOnClickListener(v -> {
                    if (listener != null) listener.onAttachmentClick(suggestion);
                });
            } else {
                ivAttachment.setVisibility(View.GONE);
            }

            itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onSuggestionLongClick(suggestion);
                return true;
            });
        }

        private void updateStatusIcon(Suggestion.SuggestionStatus status) {
            switch (status) {
                case SENDING:
                    ivStatus.setImageResource(R.drawable.ic_access_time);
                    ivStatus.setAlpha(0.5f);
                    break;
                case SENT:
                case DELIVERED:
                    ivStatus.setImageResource(R.drawable.ic_check);
                    ivStatus.setAlpha(0.8f);
                    break;
                case READ:
                    ivStatus.setImageResource(R.drawable.ic_done_all);
                    ivStatus.setAlpha(1.0f);
                    break;
                case FAILED:
                    ivStatus.setImageResource(R.drawable.ic_error);
                    ivStatus.setAlpha(1.0f);
                    break;
            }
        }
    }

    private static final DiffUtil.ItemCallback<Suggestion> DIFF_CALLBACK = new DiffUtil.ItemCallback<Suggestion>() {
        @Override
        public boolean areItemsTheSame(@NonNull Suggestion oldItem, @NonNull Suggestion newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Suggestion oldItem, @NonNull Suggestion newItem) {
            return Objects.equals(oldItem.getText(), newItem.getText()) &&
                   Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                   Objects.equals(oldItem.getTimestamp(), newItem.getTimestamp()) &&
                   Objects.equals(oldItem.getAttachmentUrl(), newItem.getAttachmentUrl()) &&
                   Objects.equals(oldItem.getAttachmentType(), newItem.getAttachmentType());
        }
    };
}
