package com.example.smartair;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.smartair.PageItem;
import com.example.smartair.R;

import java.util.ArrayList;
import java.util.List;

public class TechniqueActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private StepsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technique);

        viewPager = findViewById(R.id.viewPager);

        // prepare pages: 5 steps + 1 youtube page
        List<PageItem> pages = new ArrayList<>();

        // step images should be in res/drawable/step1.jpg ... step5.jpg
        pages.add(new PageItem(R.drawable.step1, "Step 1 — Get ready: Seal the mouthpiece\n\nImagine your little rocket would leak if air slips out. Put your lips tightly around the mouthpiece—no gaps."));
        pages.add(new PageItem(R.drawable.step2, "Step 2 — Empty your lungs: Blow out slowly\n\nBefore pressing your rocket, breathe out all the air like blowing out a birthday candle."));
        pages.add(new PageItem(R.drawable.step3, "Step 3 — Fire! Inhale deeply while pressing\n\nPress the inhaler quickly and deeply breathe in for 3–5 seconds so the medicine reaches the lungs."));
        pages.add(new PageItem(R.drawable.step4, "Step 4 — Hold the breath\n\nHold your breath for about 10 seconds if you can — this helps the medicine work. If not, hold as long as comfortable (at least 4–5s)."));
        pages.add(new PageItem(R.drawable.step5, "Step 5 — Wait before the next puff\n\nIf another puff is needed, wait 30–60 seconds (sing a short song!) then repeat the steps."));

        // youtube page special: imageResId = 0 means youtube page (adapter will detect)
        pages.add(new PageItem(0, "youtube:Br9irulpbsc"));

        adapter = new StepsAdapter(this, pages);
        viewPager.setAdapter(adapter);

        // Optional: set orientation and offscreen limit
        viewPager.setOffscreenPageLimit(1);
    }
}
