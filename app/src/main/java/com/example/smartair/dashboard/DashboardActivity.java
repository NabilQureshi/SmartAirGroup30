package com.example.smartair.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.example.smartair.R;
import com.google.type.DateTime;
// import com.example.smartair.data.models.PEFLog;
// import com.example.smartair.data.models.RescueLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {
    private TextView todayZoneText, lastRescueText, weeklyRescueText;
    private Button exportButton;
    private Button trendTimelineToggle;
    private LineChart trendChart;

    private String currentChildId;
    private String currentZone;
    private DateTime lastRescueTime;
    private int weeklyRescueCount;
    private List<Entry> trendSevenDays;
    private List<Entry> trendThirtyDays;
    private boolean sevenDayToggle = true;
    private String alert;

    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeViews();
    }

    private void initializeViews() {
        trendChart = findViewById(R.id.trendChart);
        exportButton = findViewById(R.id.exportButton);
        trendTimelineToggle = findViewById(R.id.trendTimelineToggle);
        todayZoneText = findViewById(R.id.todayZoneText);
        lastRescueText = findViewById(R.id.lastRescueText);
        weeklyRescueText = findViewById(R.id.weeklyRescueText);
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
    }
}
