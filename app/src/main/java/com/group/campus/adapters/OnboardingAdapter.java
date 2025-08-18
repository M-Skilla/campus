package com.group.campus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group.campus.R;
import com.group.campus.models.OnboardingItem;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageOnboarding;

        private TextView headerOnboarding, titleOnboarding, descOnboarding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

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

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
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
    }

    @Override
    public int getItemCount() {
        return this.onboardingItems.size();
    }
}
