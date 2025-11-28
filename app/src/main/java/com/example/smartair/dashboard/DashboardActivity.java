package com.example.smartair.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.child_managent.ChildAdapter;
import com.example.smartair.child_managent.ManageChildActivity;
import com.example.smartair.child_managent.ViewChildrenActivity;
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
    private TextView todayZoneText, lastRescueText, weeklyRescueText, title;
    private Button exportButton;
    private Button trendTimelineToggle;

    private Button childSettings;
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
    private String parentId, childId;
    private String username, password, name, dob, notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeViews();
        initializeFirebase();

        childId = getIntent().getStringExtra("childId");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        name = getIntent().getStringExtra("name");
        dob = getIntent().getStringExtra("dob");
        notes = getIntent().getStringExtra("notes");
        exportButton.setOnClickListener(v -> generateProviderReport());
        trendTimelineToggle.setOnClickListener(v -> toggleTrendRange());

        if (childId == null) {
            Toast.makeText(this, "Error: No child selected.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        title.setText("Hi " + name + "!");

        loadDashboardData();
        setupRealTimeListeners();

        childSettings.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ManageChildActivity.class);
            intent.putExtra("childId", childId);
            intent.putExtra("username", username);
            intent.putExtra("password", password);
            intent.putExtra("name", name);
            intent.putExtra("dob", dob);
            intent.putExtra("notes", notes);
            startActivity(intent);
        });
    }

    private void debugFirebaseStructure() {
        database.collection("users")
                .document(parentId)
                .collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    Log.d("DEBUG", "Child data: " + doc.getData());

                    // Check what subcollections exist
                    database.collection("users")
                            .document(parentId)
                            .collection("children")
                            .document(childId)
                            .collection("medicineLogs")
                            .limit(1)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (!snapshot.isEmpty()) {
                                    Log.d("DEBUG", "Medicine log sample: " + snapshot.getDocuments().get(0).getData());
                                }
                            });
                });
    }

    private void toggleTrendRange() {
    }

    private void generateProviderReport() {
    }

    private void initializeViews() {
        childSettings = findViewById(R.id.childSettings);
        trendChart = findViewById(R.id.trendChart);
        exportButton = findViewById(R.id.exportButton);
        trendTimelineToggle = findViewById(R.id.trendTimelineToggle);
        todayZoneText = findViewById(R.id.todayZoneText);
        lastRescueText = findViewById(R.id.lastRescueText);
        weeklyRescueText = findViewById(R.id.weeklyRescueText);
        title = findViewById(R.id.title);
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void loadDashboardData() {
        loadTodaysZone();
        loadLastRescueTime();
        loadWeeklyRescueCount();
        loadTrendData();
    }

    private void loadTrendData() {

    }

    private void loadWeeklyRescueCount() {
    }

    private void loadLastRescueTime() {
    }

    private void loadTodaysZone() {
    }

    private void setupRealTimeListeners() {
    }
}
