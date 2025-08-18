package com.group.campus.adapters;

import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group.campus.R;
import com.group.campus.models.OnboardingItem;
import com.group.campus.utils.OnItemClickListener;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageOnboarding;

        private Button continueButton;

        private TextView headerOnboarding, titleOnboarding, descOnboarding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            continueButton = itemView.findViewById(R.id.continueButton);
            imageOnboarding = itemView.findViewById(R.id.imageOnboarding);
            headerOnboarding = itemView.findViewById(R.id.headerOnboarding);
            titleOnboarding = itemView.findViewById(R.id.titleOnboarding);
            descOnboarding = itemView.findViewById(R.id.descOnboarding);
        }

        public void setOnboardingData(OnboardingItem item) {
            Glide.with(itemView.getContext())
                    .load(item.getImage())
                    .into(imageOnboarding);

            titleOnboarding.setText(item.getTitle());
            headerOnboarding.setText(item.getHeader());
            descOnboarding.setText(item.getDescription());
        }
    }

    private List<OnboardingItem> onboardingItems;

    private OnItemClickListener listener;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems, OnItemClickListener listener) {
        this.onboardingItems = onboardingItems;
        this.listener = listener;
    }

    public void setOnboardingItems(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_onboarding, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setOnboardingData(onboardingItems.get(position));
       if (listener != null && position == getItemCount() - 1) {
           holder.continueButton.setVisibility(VISIBLE);
           holder.continueButton
                   .setOnClickListener(v -> {
                        listener.onClick(v);
                   });
       }
    }

    @Override
    public int getItemCount() {
        return this.onboardingItems.size();
    }
}
