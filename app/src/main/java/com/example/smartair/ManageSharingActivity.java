package com.example.smartair;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ManageSharingActivity extends AppCompatActivity {

    private Switch switchRescue, switchController, switchSymptoms, switchTriggers;
    private Switch switchPEF, switchTriage, switchCharts;
    private ProgressBar loadingIndicator;

    private TextView textSaving;

    private String parentId, childId;
    private FirebaseFirestore db;
    private DocumentReference docRef;

    private boolean isLoading = true;

    private Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable saveRunnable;
    private static final long DEBOUNCE_DELAY = 800; //remember to check if i need to change the value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sharing);

        db = FirebaseFirestore.getInstance();
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        childId = getIntent().getStringExtra("childId");

        if (childId == null) {
            Toast.makeText(this, "Error: No child selected.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        docRef = db.collection("parents")
                .document(parentId)
                .collection("children")
                .document(childId)
                .collection("settings")
                .document("sharing");

        switchRescue = findViewById(R.id.switchRescueLogs);
        switchController = findViewById(R.id.switchController);
        switchSymptoms = findViewById(R.id.switchSymptoms);
        switchTriggers = findViewById(R.id.switchTriggers);
        switchPEF = findViewById(R.id.switchPEF);
        switchTriage = findViewById(R.id.switchTriage);
        switchCharts = findViewById(R.id.switchCharts);

        loadingIndicator = findViewById(R.id.loadingIndicator);
        textSaving = findViewById(R.id.textSaving);
        saveRunnable = this::saveAllSettings;
        loadExistingSettings();
    }

    private void loadExistingSettings() {
        loadingIndicator.setVisibility(View.VISIBLE);
        textSaving.setVisibility(View.GONE);
        isLoading = true;

        docRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                switchRescue.setChecked(Boolean.TRUE.equals(doc.getBoolean("rescueLogs")));
                switchController.setChecked(Boolean.TRUE.equals(doc.getBoolean("controller")));
                switchSymptoms.setChecked(Boolean.TRUE.equals(doc.getBoolean("symptoms")));
                switchTriggers.setChecked(Boolean.TRUE.equals(doc.getBoolean("triggers")));
                switchPEF.setChecked(Boolean.TRUE.equals(doc.getBoolean("pef")));
                switchTriage.setChecked(Boolean.TRUE.equals(doc.getBoolean("triage")));
                switchCharts.setChecked(Boolean.TRUE.equals(doc.getBoolean("charts")));
            }

            isLoading = false;
            loadingIndicator.setVisibility(View.GONE);
            setupToggleListeners();

        }).addOnFailureListener(e -> {
            isLoading = false;
            loadingIndicator.setVisibility(View.GONE);
            setupToggleListeners();
            Toast.makeText(this, "Failed to load settings.", Toast.LENGTH_LONG).show();
        });
    }

    private void setupToggleListeners() {
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (!isLoading) {
                // Show inline “Saving…”
                textSaving.setVisibility(View.VISIBLE);

                // Reset debounce
                debounceHandler.removeCallbacks(saveRunnable);
                debounceHandler.postDelayed(saveRunnable, DEBOUNCE_DELAY);
            }
        };

        switchRescue.setOnCheckedChangeListener(listener);
        switchController.setOnCheckedChangeListener(listener);
        switchSymptoms.setOnCheckedChangeListener(listener);
        switchTriggers.setOnCheckedChangeListener(listener);
        switchPEF.setOnCheckedChangeListener(listener);
        switchTriage.setOnCheckedChangeListener(listener);
        switchCharts.setOnCheckedChangeListener(listener);
    }

    private void saveAllSettings() {

        Map<String, Object> data = new HashMap<>();
        data.put("rescueLogs", switchRescue.isChecked());
        data.put("controller", switchController.isChecked());
        data.put("symptoms", switchSymptoms.isChecked());
        data.put("triggers", switchTriggers.isChecked());
        data.put("pef", switchPEF.isChecked());
        data.put("triage", switchTriage.isChecked());
        data.put("charts", switchCharts.isChecked());

        docRef.set(data)
                .addOnSuccessListener(a -> {
                    textSaving.setVisibility(View.VISIBLE);
                    textSaving.setText("All changes saved!");
                    textSaving.postDelayed(() -> {
                        textSaving.setVisibility(View.GONE);
                        textSaving.setText("Saving...");
                    }, 1500);
                })
                .addOnFailureListener(e -> {
                    textSaving.setVisibility(View.GONE);
                    Toast.makeText(this, "Error saving settings", Toast.LENGTH_SHORT).show();
                });
    }

        @Override
    protected void onDestroy() {
        super.onDestroy();
        debounceHandler.removeCallbacks(saveRunnable);
    }
}
