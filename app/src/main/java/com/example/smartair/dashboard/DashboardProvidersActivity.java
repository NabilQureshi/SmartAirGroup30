package com.example.smartair.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import java.util.Objects;

public class DashboardProvidersActivity extends AppCompatActivity {
    private TextView todayZoneText, lastRescueText, weeklyRescueText, title;
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
    private String parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String childId;
    private String username, password, name, dob, notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_providers);

        childId = getIntent().getStringExtra("childId");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        name = getIntent().getStringExtra("name");
        dob = getIntent().getStringExtra("dob");
        notes = getIntent().getStringExtra("notes");

        initializeViews();

        exportButton.setOnClickListener(v -> generateProviderReport());
        trendTimelineToggle.setOnClickListener(v -> toggleTrendRange());

        initializeFirebase();
        loadDashboardData();
        setupRealTimeListeners();
        checkFirebaseStructure();

        //checkForAlerts();

        if (childId == null) {
            Toast.makeText(this, "Error: No child selected.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        title.setText(name + "'s Dashboard");
    }

    private void checkForAlerts() {
        checkRedZoneAlert();
        checkRapidRescueAlert();
        checkWorseAfterDoseAlert();
        checkTriageEscalationAlert();
        checkLowInventoryAlert();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            checkForAlerts();
        }
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();

        Toast.makeText(this, title + ": " + message, Toast.LENGTH_LONG).show();
    }

    private void checkLowInventoryAlert() {
        // Dealt with elsewhere
    }


    private void checkTriageEscalationAlert() {
        // Dealt with elsewhere
    }

    private void checkWorseAfterDoseAlert() {
        database.collection("users")
                .document(childId)
                .collection("prepost_checks")
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        for (DocumentSnapshot doc : query) {
                            Long timestamp = doc.getLong("timestamp");
                            if (Objects.equals(doc.getString("result"), "worse") && Objects.equals(doc.getString("when"), "before")  && isFromToday(timestamp)) {
                                showAlert("Worse After Dose for " + childId,
                                        "Symptoms worsened after medication. Please monitor your child closely!");
                                return;
                            }
                        }
                    }
                });
    }

    private void checkRapidRescueAlert() {
        Calendar threeHoursAgo = Calendar.getInstance();
        threeHoursAgo.add(Calendar.HOUR_OF_DAY, -3);

        database.collection("users")
                .document(childId)
                .collection("medicine_logs")
                .whereEqualTo("type", "rescue")
                .whereGreaterThanOrEqualTo("timestamp", threeHoursAgo.getTimeInMillis())
                .get()
                .addOnSuccessListener(query -> {
                    if (query.size() >= 3) {
                        showAlert("Rapid Rescue Alert for ",
                                "3+ rescue uses in 3 hours. Consider contacting provider.");
                    }
                });
    }

    private void checkRedZoneAlert() {
        database.collection("users")
                .document(childId)
                .get()
                .addOnSuccessListener(query -> {
                    Long timestamp = query.getLong("timestamp");
                    String pbZone = query.getString("latestZoneState");
                    if (Objects.equals(pbZone, "RED") && isFromToday(timestamp)) {
                        showAlert("Red Zone for " + childId,
                                "Your child is in the Red Zone today. Check action plan immediately!");
                    }
                });
    }


    private void toggleTrendRange() {
    }

    private void generateProviderReport() {
    }

    private void initializeViews() {
        todayZoneText = findViewById(R.id.todayZoneText);
        lastRescueText = findViewById(R.id.lastRescueText);
        weeklyRescueText = findViewById(R.id.weeklyRescueText);
        title = findViewById(R.id.title);
        exportButton = findViewById(R.id.exportButton);
        trendTimelineToggle = findViewById(R.id.trendTimelineToggle);
        trendChart = findViewById(R.id.trendChart);
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
        database.collection("users")
                .document(childId)
                .collection("pef_entries")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(30)
                .get()
                .addOnSuccessListener(query -> {
                    if(query.isEmpty()) {
                        setupBasicChart();
                        return;
                    }

                    List<Entry> entries = new ArrayList<>();
                    int index = 0;

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Long pefValue = doc.getLong("pefValue");

                        if (pefValue != null) {
                            entries.add(new Entry(index, pefValue));

                            index++;
                        }
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "PEF Trend");
                    dataSet.setColor(Color.parseColor("#2196F3"));
                    dataSet.setLineWidth(3f);
                    dataSet.setCircleColor(Color.parseColor("#FF9800"));
                    dataSet.setCircleRadius(4f);
                    dataSet.setDrawValues(false);

                    LineData lineData = new LineData(dataSet);
                    trendChart.setData(lineData);

                    trendChart.getDescription().setEnabled(false);
                    trendChart.getAxisRight().setEnabled(false);
                    trendChart.invalidate();
                })
                .addOnFailureListener(e -> {
                    Log.e("Chart", "Error loading chart data", e);
                    setupBasicChart();
                });
    }

    private void loadWeeklyRescueCount() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long weekAgo = cal.getTimeInMillis();

        database.collection("users")
                .document(childId)
                .collection("medicine_logs")
                //.whereEqualTo("type", "Rescue")
                //.whereGreaterThanOrEqualTo("timestamp", weekAgo)
                .get()
                .addOnSuccessListener(query -> {
                    int count = query.size();
                    weeklyRescueText.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {
                    weeklyRescueText.setText("Error");
                    Log.e("Dashboard", "Error loading weekly rescues", e);
                });;
    }

    private void loadLastRescueTime() {
        database.collection("users")
                .document(childId)
                .collection("medicine_logs")
                //.whereEqualTo("type", "Rescue")
                //.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        Long timestamp = doc.getLong("timestamp");

                        String timeDisplay = formatRescueTime(timestamp);
                        lastRescueText.setText(timeDisplay);
                    } else {
                        lastRescueText.setText("Never");
                    }
                })
                .addOnFailureListener(e -> {
                    lastRescueText.setText("Error loading");
                });
    }

    private void setupBasicChart() {
        // Just show some sample data to prove chart works
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 300f));
        entries.add(new Entry(2, 350f));
        entries.add(new Entry(3, 320f));
        entries.add(new Entry(4, 370f));

        LineDataSet dataSet = new LineDataSet(entries, "PEF");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#FF9800"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        trendChart.setData(new LineData(dataSet));
        trendChart.getDescription().setEnabled(false);
        trendChart.invalidate();
    }

    private void loadTodaysZone() {
        database.collection("users")
                .document(childId)
                .get()
                .addOnSuccessListener(query -> {
                    String pb = String.valueOf(query.getLong("latestZonePercent"));
                    if (query.getLong("latestZonePercent") == null) {
                        pb = "N/A";
                    }
                    String pbZone = query.getString("latestZoneState");
                    if (pbZone == null) {
                        pbZone = "No Zone";
                    }
                    todayZoneText.setText(pbZone + "(" + pb + "%)");
                })
                .addOnFailureListener(e -> {
                    todayZoneText.setText("Error");
                });
    }

    private void checkFirebaseStructure() {
        Log.d("DEBUG", "Checking Firebase structure for child: " + childId);

        // Check if child document exists
        database.collection("users")
                .document("2NJQsGjfAHMDkT3uAp4jYmEvYD62")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d("DEBUG", "Child document exists: " + doc.getData());
                    } else {
                        Log.d("DEBUG", "Child document does NOT exist");
                    }
                });

        // Check what collections exist under child
        database.collection("users")
                .document("2NJQsGjfAHMDkT3uAp4jYmEvYD62")
                .collection("medicine_logs")
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Log.d("DEBUG", "medicine_logs exists. Sample: " + snapshot.getDocuments().get(0).getData());
                    } else {
                        Log.d("DEBUG", "medicine_logs collection exists but is empty");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("DEBUG", "medicine_logs collection does NOT exist or error: " + e.getMessage());
                });

        // Check pef_entries collection
        database.collection("users")
                .document("2NJQsGjfAHMDkT3uAp4jYmEvYD62")
                .collection("pef_entries")
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Log.d("DEBUG", "pef_entries exists. Sample: " + snapshot.getDocuments().get(0).getData());
                    } else {
                        Log.d("DEBUG", "pef_entries collection exists but is empty");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("DEBUG", "pef_entries collection does NOT exist or error: " + e.getMessage());
                });
    }

    private String formatRescueTime(Long timestamp) {
        if (timestamp == null) return "Unknown";

        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long minutes = diff / (1000 * 60);
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }

    private boolean isFromToday(Long timestamp) {
        if (timestamp == null) return false;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long startOfToday = today.getTimeInMillis();

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);
        long startOfTomorrow = tomorrow.getTimeInMillis();

        return timestamp >= startOfToday && timestamp < startOfTomorrow;
    }

    private void setupRealTimeListeners() {
    }
}
