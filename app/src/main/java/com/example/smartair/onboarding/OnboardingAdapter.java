package com.example.smartair.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.model.OnboardingPage;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingPage> pages;

    public OnboardingAdapter(List<OnboardingPage> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingPage page = pages.get(position);
        holder.bind(page);
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageIcon;
        private TextView textTitle;
        private TextView textDescription;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIcon = itemView.findViewById(R.id.imageIcon);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
        }

        public void bind(OnboardingPage page) {
            textTitle.setText(page.getTitle());
            textDescription.setText(page.getDescription());
            imageIcon.setImageResource(page.getIconResId());
        }
    }
}