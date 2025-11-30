package com.example.smartair.dashboard;

import android.content.Intent;
import android.graphics.Color;
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
    private String parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String childId;
    private String username, password, name, dob, notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeViews();
        initializeFirebase();
        loadDashboardData();
        setupRealTimeListeners();
        checkFirebaseStructure();
        setupBasicChart();

        childId = getIntent().getStringExtra("childId");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        name = getIntent().getStringExtra("name");
        dob = getIntent().getStringExtra("dob");
        notes = getIntent().getStringExtra("notes");
        exportButton.setOnClickListener(v -> generateProviderReport());
        trendTimelineToggle.setOnClickListener(v -> toggleTrendRange());

        //checkForAlerts();

        if (childId == null) {
            Toast.makeText(this, "Error: No child selected.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        title.setText("Hi " + name + "!");

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

    private void checkForAlerts() {
        checkRedZoneAlert();
        checkRapidRescueAlert();
        checkWorseAfterDoseAlert();
        checkTriageEscalationAlert();
        checkLowInventoryAlert();

    }

    private void checkLowInventoryAlert() {
    }

    private void checkTriageEscalationAlert() {
    }

    private void checkWorseAfterDoseAlert() {
    }

    private void checkRapidRescueAlert() {
    }

    private void checkRedZoneAlert() {
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
        todayZoneText = findViewById(R.id.todayZoneText);
        lastRescueText = findViewById(R.id.lastRescueText);
        weeklyRescueText = findViewById(R.id.weeklyRescueText);
        title = findViewById(R.id.title);
        exportButton = findViewById(R.id.exportButton);
        trendTimelineToggle = findViewById(R.id.trendTimelineToggle);
        childSettings = findViewById(R.id.childSettings);
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

    }

    private void loadWeeklyRescueCount() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long weekAgo = cal.getTimeInMillis();

        database.collection("users")
                .document("2NJQsGjfAHMDkT3uAp4jYmEvYD62")
                .collection("medicine_logs")
                //.whereEqualTo("type", "Rescue")
                //.whereGreaterThanOrEqualTo("timestamp", weekAgo)
                .get()
                .addOnSuccessListener(query -> {
                    int count = query.size();
                    weeklyRescueText.setText("Weekly Rescues: " + count);
                })
                .addOnFailureListener(e -> {
                    weeklyRescueText.setText("Weekly Rescues: Error");
                    Log.e("Dashboard", "Error loading weekly rescues", e);
                });;
    }

    private void loadLastRescueTime() {
        database.collection("users")
                .document("2NJQsGjfAHMDkT3uAp4jYmEvYD62")
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
                        lastRescueText.setText("Last Rescue: " + timestamp);
                    } else {
                        lastRescueText.setText("Last Rescue: Never");
                    }
                })
                .addOnFailureListener(e -> {
                    lastRescueText.setText("Last Rescue: Error loading");
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
        dataSet.setColor(Color.BLUE);

        trendChart.setData(new LineData(dataSet));
        trendChart.getDescription().setEnabled(false);
        trendChart.invalidate();
    }

    private void loadTodaysZone() {
        database.collection("users")
                .document("2NJQsGjfAHMDkT3uAp4jYmEvYD62")
                .collection("pef_entries")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        Long timestamp = doc.getLong("timestamp");
                        Long currentPEF = doc.getLong("pefValue");
                        if (isFromToday(timestamp)) {
                            todayZoneText.setText("Today's Zone (not quite yet): " + currentPEF);
                        } else {
                            todayZoneText.setText("Today's Zone: No PEF data today");
                        }
                    } else {
                        todayZoneText.setText("Today's Zone: " + childId);
                    }
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
