package com.example.smartair.checkin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SymptomHistoryActivity extends AppCompatActivity {

    private TextView tvChildName;
    private Spinner spinnerSymptomFilter, spinnerTriggerFilter;
    private Button btnStartDate, btnEndDate, btnExportPDF, btnExportCSV;
    private RecyclerView recyclerHistory;

    private HistoryAdapter adapter;
    private final List<HistoryEntry> allEntries = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_history);

        childId = getIntent().getStringExtra("childId");
        if (childId == null) {
            Toast.makeText(this, "No child selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupSpinners();
        setupRecycler();
        setupExportButtons();

        loadLastSixMonths();
    }

    private void bindViews() {
        tvChildName = findViewById(R.id.tvChildName);
        spinnerSymptomFilter = findViewById(R.id.spinnerSymptomFilter);
        spinnerTriggerFilter = findViewById(R.id.spinnerTriggerFilter);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnExportPDF = findViewById(R.id.btnExportPDF);
        btnExportCSV = findViewById(R.id.btnExportCSV);
        recyclerHistory = findViewById(R.id.recyclerHistory);

        tvChildName.setText("Child ID: " + childId);
    }

    private void setupSpinners() {
        // For now just "All" so it compiles and runs without complex logic
        String[] symptomOptions = {"All"};
        String[] triggerOptions = {"All"};

        ArrayAdapter<String> symptomAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, symptomOptions);
        symptomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSymptomFilter.setAdapter(symptomAdapter);

        ArrayAdapter<String> triggerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, triggerOptions);
        triggerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTriggerFilter.setAdapter(triggerAdapter);
    }

    private void setupRecycler() {
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(new ArrayList<>());
        recyclerHistory.setAdapter(adapter);
    }

    private void setupExportButtons() {
        btnExportPDF.setOnClickListener(v ->
                Toast.makeText(this, "PDF export not implemented yet", Toast.LENGTH_SHORT).show());

        btnExportCSV.setOnClickListener(v ->
                Toast.makeText(this, "CSV export not implemented yet", Toast.LENGTH_SHORT).show());

        btnStartDate.setOnClickListener(v ->
                Toast.makeText(this, "Date picker not implemented yet", Toast.LENGTH_SHORT).show());

        btnEndDate.setOnClickListener(v ->
                Toast.makeText(this, "Date picker not implemented yet", Toast.LENGTH_SHORT).show());
    }

    private void loadLastSixMonths() {
        long sixMonthsMillis = 1000L * 60 * 60 * 24 * 30 * 6;
        long cutoff = System.currentTimeMillis() - sixMonthsMillis;

        db.collection("symptomCheckIns")
                .document(childId)
                .collection("daily")
                .whereGreaterThanOrEqualTo("lastUpdated", cutoff)
                .get()
                .addOnSuccessListener(snap -> {
                    allEntries.clear();

                    for (QueryDocumentSnapshot d : snap) {
                        String dateKey = d.getId(); // yyyy-MM-dd
                        String night = d.getString("nightWaking");
                        String act = d.getString("activityLimits");
                        String cough = d.getString("cough");
                        String chest = d.getString("chestPain");
                        String submittedBy = d.getString("submittedBy");

                        Object trigObj = d.get("triggers");
                        String triggers = trigObj != null ? trigObj.toString() : "None";

                        String summary = "Night: " + safe(night)
                                + " | Activity: " + safe(act)
                                + " | Cough: " + safe(cough)
                                + " | Chest: " + safe(chest);

                        allEntries.add(new HistoryEntry(dateKey, summary, triggers, submittedBy));
                    }

                    adapter.update(allEntries);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error loading history: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private String safe(String s) {
        return s == null ? "N/A" : s;
    }
}
