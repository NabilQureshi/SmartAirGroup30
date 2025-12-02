package com.example.smartair.model;

public class OnboardingPage {
    private String title;
    private String description;
    private int iconResId;

    public OnboardingPage(String title, String description, int iconResId) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResId() {
        return iconResId;
    }
}