package com.group.campus.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.group.campus.R;
import com.group.campus.models.Messages;

import java.util.List;

import io.noties.markwon.Markwon;

public class AIChatAdapter extends RecyclerView.Adapter<AIChatAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvPrompt, tvResponse;

        LinearLayout promptSection, responseSection;

        LottieAnimationView lottieLoading;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrompt = itemView.findViewById(R.id.tv_prompt);
            tvResponse = itemView.findViewById(R.id.tv_response);
            lottieLoading = itemView.findViewById(R.id.lottie_loading);
            promptSection = itemView.findViewById(R.id.promptSection);
            responseSection = itemView.findViewById(R.id.responseSection);
        }
    }


    private List<Messages> messages;

    private boolean onLoading;
    public AIChatAdapter(List<Messages> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvPrompt.setText(messages.get(position).getPrompt());
        Messages message = messages.get(position);
        if (message.getResponse() == null || message.getResponse().isEmpty()) {
            holder.lottieLoading.setVisibility(View.VISIBLE);
            holder.responseSection.setVisibility(View.GONE);
        } else {
            holder.lottieLoading.setVisibility(View.GONE);
            holder.responseSection.setVisibility(View.VISIBLE);
            Markwon markwon = Markwon.create(holder.tvResponse.getContext());
            markwon.setMarkdown(holder.tvResponse, message.getResponse());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {

            Bundle bundle = (Bundle) payloads.get(0);
            if (bundle.containsKey("response")) {
                holder.lottieLoading.setVisibility(View.GONE);
                holder.responseSection.setVisibility(View.VISIBLE);
                Markwon markwon = Markwon.create(holder.tvResponse.getContext());
                markwon.setMarkdown(holder.tvResponse, bundle.getString("response"));
            }
        } else {
            // Full bind
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<Messages> messages) {
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    public void addMessage(Messages message) {
        this.messages.add(message);
        notifyItemInserted(getItemCount() - 1);
    }

    public void updateMessageNow(Messages message) {
        Bundle payload = new Bundle();
        payload.putString("response", message.getResponse());
        notifyItemChanged(getItemCount() - 1, payload);
    }
}
