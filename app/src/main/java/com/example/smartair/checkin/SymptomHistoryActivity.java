package com.example.smartair.checkin;

import android.Manifest;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.smartair.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SymptomHistoryActivity extends AppCompatActivity {

    private TextView tvChildName;
    private Button btnStartDate, btnEndDate, btnApplyFilters, btnExportPDF, btnExportCSV;
    private Spinner spinnerSymptomFilter, spinnerTriggerFilter;
    private LinearLayout historyContainer;

    private FirebaseFirestore db;
    private String childId;

    private long startDateMillis;
    private long endDateMillis;

    private final SimpleDateFormat displayDateFormatter =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private final List<Map<String, Object>> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_history);

        // === STORAGE PERMISSION for Export ===
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    1
            );
        }

        db = FirebaseFirestore.getInstance();
        childId = getIntent().getStringExtra("childId");

        if (childId == null || childId.isEmpty()) {
            Toast.makeText(this, "Missing childId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvChildName = findViewById(R.id.tvChildName);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnApplyFilters = findViewById(R.id.btnApplyFilters);
        btnExportPDF = findViewById(R.id.btnExportPDF);
        btnExportCSV = findViewById(R.id.btnExportCSV);
        spinnerSymptomFilter = findViewById(R.id.spinnerSymptomFilter);
        spinnerTriggerFilter = findViewById(R.id.spinnerTriggerFilter);
        historyContainer = findViewById(R.id.historyContainer);

        setupDefaults();
        setupSpinners();
        setupButtons();
        loadChildName();
        loadHistory();
    }

    private void setupDefaults() {
        Calendar cal = Calendar.getInstance();
        endDateMillis = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -6);
        startDateMillis = cal.getTimeInMillis();
        btnStartDate.setText("From: " + displayDateFormatter.format(new Date(startDateMillis)));
        btnEndDate.setText("To: " + displayDateFormatter.format(new Date(endDateMillis)));
    }

    private void setupSpinners() {
        spinnerSymptomFilter.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Any Symptom","Night Waking","Activity Limits","Cough/Wheeze","Chest Pain"}));

        spinnerTriggerFilter.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Any Trigger","Exercise","Cold Air","Dust/Pets","Smoke","Illness","Perfume/Odors"}));
    }

    private void setupButtons() {
        btnStartDate.setOnClickListener(v -> pickDate(true));
        btnEndDate.setOnClickListener(v -> pickDate(false));
        btnApplyFilters.setOnClickListener(v -> loadHistory());
        btnExportPDF.setOnClickListener(v -> exportPdf());
        btnExportCSV.setOnClickListener(v -> exportCsv());
    }

    private void pickDate(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(isStart ? startDateMillis : endDateMillis);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(y, m, d);
            if (isStart) {
                startDateMillis = chosen.getTimeInMillis();
                btnStartDate.setText("From: " + displayDateFormatter.format(chosen.getTime()));
            } else {
                chosen.set(Calendar.HOUR_OF_DAY,23);
                chosen.set(Calendar.MINUTE,59);
                chosen.set(Calendar.SECOND,59);
                endDateMillis = chosen.getTimeInMillis();
                btnEndDate.setText("To: " + displayDateFormatter.format(chosen.getTime()));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void loadChildName() {
        db.collection("users").document(childId).get()
                .addOnSuccessListener(doc -> tvChildName.setText("Child: " +
                        (doc.exists() ? doc.getString("name") : childId)));
    }

    private void loadHistory() {
        historyContainer.removeAllViews();
        historyList.clear();

        String symptomFilter = spinnerSymptomFilter.getSelectedItem().toString();
        String triggerFilter = spinnerTriggerFilter.getSelectedItem().toString();

        db.collection("symptomCheckIns").document(childId)
                .collection("daily")
                .whereGreaterThanOrEqualTo("lastUpdated", startDateMillis)
                .whereLessThanOrEqualTo("lastUpdated", endDateMillis)
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        addEntry("No records.");
                        return;
                    }

                    for (DocumentSnapshot doc : snap) {
                        Map<String, Object> d = doc.getData();
                        if (d == null) continue;

                        Date date = new Date((Long) d.get("lastUpdated"));

                        // ===== SYMPTOM FILTER (Mild/Moderate/Severe only) =====
                        if (!symptomFilter.equals("Any Symptom")) {

                            String night   = safe(d.get("nightWaking"));
                            String act     = safe(d.get("activityLimits"));
                            String cough   = safe(d.get("cough"));
                            String chest   = safe(d.get("chestPain"));

                            boolean visible =
                                    (symptomFilter.equals("Night Waking")    && isSymptomatic(night)) ||
                                            (symptomFilter.equals("Activity Limits") && isSymptomatic(act))   ||
                                            (symptomFilter.equals("Cough/Wheeze")    && isSymptomatic(cough)) ||
                                            (symptomFilter.equals("Chest Pain")      && isSymptomatic(chest));

                            if (!visible) continue;
                        }

                        String record =
                                displayDateFormatter.format(date) + "\n" +
                                        "Night Waking: " + d.get("nightWaking") + "\n" +
                                        "Activity Limits: " + d.get("activityLimits") + "\n" +
                                        "Cough/Wheeze: " + d.get("cough") + "\n" +
                                        "Chest Pain: " + d.get("chestPain") + "\n" +
                                        "Triggers: " + (d.get("triggers") == null ? "None" : d.get("triggers")) + "\n" +
                                        "Submitted By: " + d.get("submittedBy");

                        addEntry(record);

                        Map<String,Object> row = new LinkedHashMap<>();
                        row.put("date", displayDateFormatter.format(date));
                        row.put("nightWaking", d.get("nightWaking"));
                        row.put("activityLimits", d.get("activityLimits"));
                        row.put("cough", d.get("cough"));
                        row.put("chestPain", d.get("chestPain"));
                        row.put("triggers", d.get("triggers"));
                        row.put("submittedBy", d.get("submittedBy"));

                        historyList.add(row);
                    }
                });
    }

    private boolean isSymptomatic(String value) {
        if (value == null) return false;
        value = value.trim().toLowerCase();
        return value.equals("mild") || value.equals("moderate") || value.equals("severe");
    }

    private String safe(Object v) {
        return v == null ? "" : v.toString();
    }

    private void addEntry(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(15);
        t.setPadding(10,10,10,20);
        historyContainer.addView(t);
    }

    private void exportPdf() {
        if(historyList.isEmpty()) {Toast.makeText(this,"No data.",Toast.LENGTH_SHORT).show();return;}

        StringBuilder text = new StringBuilder();
        for(Map<String,Object> r : historyList){
            for(String k:r.keySet()) text.append(k).append(": ").append(r.get(k)).append("\n");
            text.append("\n");
        }
        ExportHelper.savePdfFromText(this,text.toString(),"symptom_report");
    }

    private void exportCsv() {
        if(historyList.isEmpty()) {Toast.makeText(this,"No data.",Toast.LENGTH_SHORT).show();return;}
        ExportHelper.saveCsv(this,historyList,"symptom_history");
    }
}
