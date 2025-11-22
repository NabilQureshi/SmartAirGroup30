package com.example.smartair;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class StepsAdapter extends FragmentStateAdapter {

    private List<PageItem> pages;

    public StepsAdapter(@NonNull FragmentActivity fragmentActivity, List<PageItem> pages) {
        super(fragmentActivity);
        this.pages = pages;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        PageItem item = pages.get(position);
        if (item.imageRes == 0 && item.text != null && item.text.startsWith("youtube:")) {
            String videoId = item.text.substring("youtube:".length());
            return com.example.smartair.YoutubePageFragment.newInstance(videoId);
        } else {
            return StepFragment.newInstance(item.imageRes, item.text);
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }
}

