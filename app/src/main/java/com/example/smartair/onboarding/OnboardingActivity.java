package com.example.smartair.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.smartair.R;
import com.example.smartair.homepages.HomepageActivity;
import com.example.smartair.homepages.HomepageParentsActivity;
import com.example.smartair.homepages.HomepageProvidersActivity;
import com.example.smartair.model.OnboardingPage;
import com.example.smartair.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private Button btnSkip;
    private List<OnboardingPage> pages;
    private SharedPrefsHelper prefsHelper;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        prefsHelper = new SharedPrefsHelper(this);
        userRole = getIntent().getStringExtra("userRole");

        if (userRole == null) {
            userRole = prefsHelper.getUserRole();
        }

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        setupOnboardingPages();

        OnboardingAdapter adapter = new OnboardingAdapter(pages);
        viewPager.setAdapter(adapter);

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < pages.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == pages.size() - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });
    }

    private void setupOnboardingPages() {
        pages = new ArrayList<>();

        pages.add(new OnboardingPage(
                "Welcome to SMART AIR",
                "Your companion for managing asthma effectively and safely.",
                android.R.drawable.ic_dialog_info
        ));

        pages.add(new OnboardingPage(
                "Two Types of Medicines",
                "Rescue inhalers provide quick relief during asthma attacks. Controller medicines are taken daily to prevent symptoms.",
                android.R.drawable.ic_menu_info_details
        ));

        if ("parent".equals(userRole)) {
            pages.add(new OnboardingPage(
                    "Privacy & Control",
                    "You control what data is shared. By default, no information is shared with healthcare providers until you enable it.",
                    android.R.drawable.ic_lock_lock
            ));

            pages.add(new OnboardingPage(
                    "Manage Sharing",
                    "You can enable or disable data sharing for each child individually. Invite providers using secure 7-day codes.",
                    android.R.drawable.ic_menu_share
            ));
        } else if ("provider".equals(userRole)) {
            pages.add(new OnboardingPage(
                    "Access Patient Data",
                    "You can only view data that parents have explicitly shared with you. All access requires parent approval.",
                    android.R.drawable.ic_menu_view
            ));
        } else {
            pages.add(new OnboardingPage(
                    "Track Your Health",
                    "Log your medicines, check your peak flow, and track symptoms to stay healthy.",
                    android.R.drawable.ic_menu_today
            ));
        }

        pages.add(new OnboardingPage(
                "You're All Set!",
                "Let's get started managing asthma together.",
                android.R.drawable.ic_dialog_email
        ));
    }

    private void finishOnboarding() {
        prefsHelper.setOnboardingComplete(true);

        Intent intent;
        if ("parent".equals(userRole)) {
            intent = new Intent(this, HomepageParentsActivity.class);
        } else if ("provider".equals(userRole)) {
            intent = new Intent(this, HomepageProvidersActivity.class);
        } else {
            intent = new Intent(this, HomepageActivity.class);
        }

        startActivity(intent);
        finish();
    }
}