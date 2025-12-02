package com.example.smartair.dashboard;

import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.type.DateTime;

import java.text.SimpleDateFormat;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Provider dashboard showing key metrics for a selected child.
 * Data sources:
 *  - users/{childId} for latest zone fields
 *  - users/{childId}/medicine_logs for rescue/controller metrics
 *  - users/{childId}/pef_entries for trend
 *  - symptomCheckIns/{childId}/daily for symptom burden
 *  - users/{childId}/triage_sessions for notable triage incidents
 */
public class DashboardProvidersActivity extends AppCompatActivity {
    private TextView todayZoneText, lastRescueText, weeklyRescueText, title;
    private TextView controllerAdherenceText, symptomBurdenText, triageIncidentsText;
    private Button exportButton;
    private Button trendTimelineToggle;
    private LineChart trendChart;

    private List<Entry> trendSevenDays = new ArrayList<>();
    private List<Entry> trendThirtyDays = new ArrayList<>();
    private boolean sevenDayToggle = true;

    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private String parentId;
    private String childId;
    private String username, password, name, dob, notes;
    private boolean shareRescue = true;
    private boolean shareController = true;
    private boolean shareSymptoms = true;
    private boolean shareTriggers = true;
    private boolean sharePef = true;
    private boolean shareTriage = true;
    private boolean shareCharts = true;

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
        if (childId == null) {
            Toast.makeText(this, "Error: No child selected.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        title.setText(name + "'s Dashboard");
        loadSharingSettings();
    }

    private void initializeViews() {
        todayZoneText = findViewById(R.id.todayZoneText);
        lastRescueText = findViewById(R.id.lastRescueText);
        weeklyRescueText = findViewById(R.id.weeklyRescueText);
        controllerAdherenceText = findViewById(R.id.controllerAdherenceText);
        symptomBurdenText = findViewById(R.id.symptomBurdenText);
        triageIncidentsText = findViewById(R.id.triageIncidentsText);
        title = findViewById(R.id.title);
        exportButton = findViewById(R.id.exportButton);
        trendTimelineToggle = findViewById(R.id.trendTimelineToggle);
        trendChart = findViewById(R.id.trendChart);
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        parentId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private void loadSharingSettings() {
        String providerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (providerId == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        database.collection("providers")
                .document(providerId)
                .collection("linkedChildren")
                .document(childId)
                .collection("settings")
                .document("sharing")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        shareRescue = Boolean.TRUE.equals(doc.getBoolean("rescueLogs"));
                        shareController = Boolean.TRUE.equals(doc.getBoolean("controller"));
                        shareSymptoms = Boolean.TRUE.equals(doc.getBoolean("symptoms"));
                        shareTriggers = Boolean.TRUE.equals(doc.getBoolean("triggers"));
                        sharePef = Boolean.TRUE.equals(doc.getBoolean("pef"));
                        shareTriage = Boolean.TRUE.equals(doc.getBoolean("triage"));
                        shareCharts = Boolean.TRUE.equals(doc.getBoolean("charts"));
                    }
                    loadDashboardData();
                })
                .addOnFailureListener(e -> {
                    // If we can't read settings, default to hiding sensitive data
                    shareRescue = shareController = shareSymptoms = shareTriggers = sharePef = shareTriage = shareCharts = false;
                    loadDashboardData();
                });
    }

    private void loadDashboardData() {
        loadTodaysZone();
        if (shareRescue) {
            loadLastRescueTime();
            loadWeeklyRescueCount();
        } else {
            lastRescueText.setText("Not shared");
            weeklyRescueText.setText("Not shared");
        }
        if (shareController) {
            loadControllerAdherence();
        } else {
            controllerAdherenceText.setText("Not shared");
        }
        if (shareSymptoms || shareTriggers) { // symptom burden depends on check-ins and triggers, tie to symptoms toggle
            loadSymptomBurden();
        } else {
            symptomBurdenText.setText("Not shared");
        }
        if (shareTriage) {
            loadTriageIncidents();
        } else {
            triageIncidentsText.setText("Not shared");
        }
        if (sharePef && shareCharts) {
            loadTrendData();
            trendChart.setVisibility(android.view.View.VISIBLE);
            trendTimelineToggle.setVisibility(android.view.View.VISIBLE);
        } else {
            trendChart.setVisibility(android.view.View.GONE);
            trendTimelineToggle.setVisibility(android.view.View.GONE);
        }
    }

    // ---------------- Dashboard tiles ----------------

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
                    todayZoneText.setText(pbZone + " (" + pb + "%)");
                })
                .addOnFailureListener(e -> todayZoneText.setText("Error"));
    }

    private void loadLastRescueTime() {
        database.collection("users")
                .document(childId)
                .collection("medicine_logs")
                .whereEqualTo("type", "Rescue")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        Long timestamp = doc.getLong("timestamp");
                        lastRescueText.setText(formatRescueTime(timestamp));
                    } else {
                        lastRescueText.setText("Never");
                    }
                })
                .addOnFailureListener(e -> lastRescueText.setText("Error"));
    }

    private void loadWeeklyRescueCount() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long weekAgo = cal.getTimeInMillis();

        database.collection("users")
                .document(childId)
                .collection("medicine_logs")
                .whereEqualTo("type", "Rescue")
                .whereGreaterThanOrEqualTo("timestamp", weekAgo)
                .get()
                .addOnSuccessListener(query -> weeklyRescueText.setText(String.valueOf(query.size())))
                .addOnFailureListener(e -> {
                    weeklyRescueText.setText("Error");
                    Log.e("Dashboard", "Error loading weekly rescues", e);
                });
    }

    private void loadControllerAdherence() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        long thirtyAgo = cal.getTimeInMillis();

        database.collection("users")
                .document(childId)
                .collection("medicine_logs")
                .whereEqualTo("type", "Controller")
                .whereGreaterThanOrEqualTo("timestamp", thirtyAgo)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        controllerAdherenceText.setText("0% (0/30 days)");
                        return;
                    }
                    Set<String> days = new HashSet<>();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Long ts = doc.getLong("timestamp");
                        if (ts != null) {
                            days.add(df.format(ts));
                        }
                    }
                    int dayCount = days.size();
                    int percent = Math.min(100, Math.round((dayCount / 30f) * 100));
                    controllerAdherenceText.setText(percent + "% (" + dayCount + "/30 days)");
                })
                .addOnFailureListener(e -> controllerAdherenceText.setText("Error"));
    }

    private void loadSymptomBurden() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        long thirtyAgo = cal.getTimeInMillis();

        database.collection("symptomCheckIns")
                .document(childId)
                .collection("daily")
                .whereGreaterThanOrEqualTo("lastUpdated", thirtyAgo)
                .get()
                .addOnSuccessListener(query -> {
                    int problemDays = 0;
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String night = doc.getString("nightWaking");
                        String activity = doc.getString("activityLimits");
                        String cough = doc.getString("cough");
                        String chest = doc.getString("chestPain");
                        if (isProblem(night) || isProblem(activity) || isProblem(cough) || isProblem(chest)) {
                            problemDays++;
                        }
                    }
                    symptomBurdenText.setText(problemDays + " problem days / 30");
                })
                .addOnFailureListener(e -> symptomBurdenText.setText("Error"));
    }

    private boolean isProblem(String value) {
        if (value == null) return false;
        String v = value.toLowerCase(Locale.getDefault());
        return !(v.contains("none") || v.contains("no"));
    }

    private void loadTriageIncidents() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        long thirtyAgo = cal.getTimeInMillis();

        database.collection("users")
                .document(childId)
                .collection("triage_sessions")
                .whereGreaterThanOrEqualTo("timestamp", thirtyAgo)
                .get()
                .addOnSuccessListener(query -> triageIncidentsText.setText(query.size() + " in last 30d"))
                .addOnFailureListener(e -> triageIncidentsText.setText("Error"));
    }

    // ---------------- Trend ----------------

    private void loadTrendData() {
        database.collection("users")
                .document(childId)
                .collection("pef_entries")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(30)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
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

                    trendThirtyDays = entries;
                    trendSevenDays = entries.size() > 7
                            ? new ArrayList<>(entries.subList(entries.size() - 7, entries.size()))
                            : new ArrayList<>(entries);

                    applyTrendData(sevenDayToggle);
                })
                .addOnFailureListener(e -> {
                    Log.e("Chart", "Error loading chart data", e);
                    setupBasicChart();
                });
    }

    private void toggleTrendRange() {
        sevenDayToggle = !sevenDayToggle;
        applyTrendData(sevenDayToggle);
        trendTimelineToggle.setText(sevenDayToggle ? "Showing 7 days" : "Showing 30 days");
    }

    private void applyTrendData(boolean sevenDays) {
        List<Entry> entries = sevenDays ? trendSevenDays : trendThirtyDays;
        if (entries == null || entries.isEmpty()) {
            setupBasicChart();
            return;
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
    }

    private void setupBasicChart() {
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

    // ---------------- Alerts ----------------

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

    private void checkLowInventoryAlert() {
        database.collection("users")
                .document(childId)
                .collection("inventory")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || snapshot.isEmpty()) return;
                    long now = System.currentTimeMillis();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Long qty = doc.getLong("dose");
                        if (qty == null) qty = doc.getLong("quantity");
                        Long expiry = doc.getLong("expiry");
                        boolean low = qty != null && qty <= 2;
                        boolean expired = expiry != null && expiry <= now;
                        if (low || expired) {
                            showAlert("Inventory Alert", "Medicine low or expired. Please review inventory.");
                            return;
                        }
                    }
                });
    }

    private void checkTriageEscalationAlert() {
        database.collection("users")
                .document(childId)
                .collection("triage_sessions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || snapshot.isEmpty()) return;
                    DocumentSnapshot doc = snapshot.getDocuments().get(0);
                    String zone = doc.getString("zoneState");
                    String severity = doc.getString("severity");
                    Boolean emergency = doc.getBoolean("emergency");
                    if ((zone != null && zone.equalsIgnoreCase("RED")) ||
                            (severity != null && severity.toLowerCase(Locale.getDefault()).contains("emergency")) ||
                            Boolean.TRUE.equals(emergency)) {
                        showAlert("Triage Escalation", "Latest triage indicates emergency/RED zone.");
                    }
                });
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
                            String when = doc.getString("when");
                            String result = doc.getString("result");
                            if (Objects.equals(result, "worse")
                                    && Objects.equals(when, "after")
                                    && isFromToday(timestamp)) {
                                showAlert("Worse After Dose for " + childId,
                                        "Symptoms worsened after medication. Please monitor closely.");
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

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();

        Toast.makeText(this, title + ": " + message, Toast.LENGTH_LONG).show();
    }

    // ---------------- Helpers ----------------

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

    private void generateProviderReport() {
        String fileName = "provider_report_" + (childId != null ? childId : "child") + ".pdf";

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4-ish
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(14f);

        int y = 60;
        int x = 40;

        paint.setTextSize(18f);
        paint.setFakeBoldText(true);
        page.getCanvas().drawText("Provider Report", x, y, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(12f);
        y += 20;
        page.getCanvas().drawText("Child: " + (name != null ? name : childId), x, y, paint);
        y += 16;
        page.getCanvas().drawText("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()), x, y, paint);
        y += 24;

        y = drawLine(page, paint, x, y, "Today's Zone", todayZoneText.getText().toString());
        y = drawLine(page, paint, x, y, "Last Rescue", lastRescueText.getText().toString());
        y = drawLine(page, paint, x, y, "Weekly Rescues", weeklyRescueText.getText().toString());
        y = drawLine(page, paint, x, y, "Controller Adherence", controllerAdherenceText.getText().toString());
        y = drawLine(page, paint, x, y, "Symptom Burden", symptomBurdenText.getText().toString());
        y = drawLine(page, paint, x, y, "Triage Incidents", triageIncidentsText.getText().toString());

        pdfDocument.finishPage(page);

        try {
            savePdfToDownloads(pdfDocument, fileName);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save report: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pdfDocument.close();
        }
    }

    private int drawLine(PdfDocument.Page page, Paint paint, int x, int y, String label, String value) {
        paint.setFakeBoldText(true);
        page.getCanvas().drawText(label + ":", x, y, paint);
        paint.setFakeBoldText(false);
        page.getCanvas().drawText(value != null ? value : "N/A", x + 180, y, paint);
        return y + 18;
    }

    private void savePdfToDownloads(PdfDocument pdfDocument, String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pdfDocument.writeTo(baos);
            byte[] data = baos.toByteArray();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new IOException("Unable to create file in Downloads");

            try (java.io.OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new IOException("No output stream for Downloads");
                out.write(data);
            }

            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);

            Toast.makeText(this, "Report saved to Downloads", Toast.LENGTH_LONG).show();
        } else {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (dir != null && !dir.exists()) dir.mkdirs();
            File pdfFile = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                pdfDocument.writeTo(fos);
            }
            Toast.makeText(this, "Report saved: " + (dir != null ? dir.getAbsolutePath() : "Downloads"), Toast.LENGTH_LONG).show();
        }
    }
}
