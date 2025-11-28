package com.example.smartair.sharing;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.smartair.R;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ManageSharingActivity extends AppCompatActivity {

    private SwitchCompat switchRescue, switchController, switchSymptoms, switchTriggers;
    private SwitchCompat switchPEF, switchTriage, switchCharts;

    private TextView tagRescue, tagController, tagSymptoms, tagTriggers;
    private TextView tagPEF, tagTriage, tagCharts;

    private ProgressBar loadingIndicator;
    private TextView textSaving;

    private String parentId, childId;
    private DocumentReference docRef;
    private FirebaseFirestore db;

    private boolean isLoading = true;

    private Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable saveRunnable;
    private static final long DEBOUNCE_DELAY = 800;

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

        tagRescue = findViewById(R.id.tagRescue);
        tagController = findViewById(R.id.tagController);
        tagSymptoms = findViewById(R.id.tagSymptoms);
        tagTriggers = findViewById(R.id.tagTriggers);
        tagPEF = findViewById(R.id.tagPEF);
        tagTriage = findViewById(R.id.tagTriage);
        tagCharts = findViewById(R.id.tagCharts);

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

                setToggleState(switchRescue, tagRescue, doc.getBoolean("rescueLogs"));
                setToggleState(switchController, tagController, doc.getBoolean("controller"));
                setToggleState(switchSymptoms, tagSymptoms, doc.getBoolean("symptoms"));
                setToggleState(switchTriggers, tagTriggers, doc.getBoolean("triggers"));
                setToggleState(switchPEF, tagPEF, doc.getBoolean("pef"));
                setToggleState(switchTriage, tagTriage, doc.getBoolean("triage"));
                setToggleState(switchCharts, tagCharts, doc.getBoolean("charts"));
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

    private void setToggleState(SwitchCompat sw, TextView tag, Boolean value) {
        boolean checked = Boolean.TRUE.equals(value);
        sw.setChecked(checked);
        tag.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    private void setupToggleListeners() {

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {

            int id = buttonView.getId();

            if (id == R.id.switchRescueLogs) tagRescue.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            else if (id == R.id.switchController) tagController.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            else if (id == R.id.switchSymptoms) tagSymptoms.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            else if (id == R.id.switchTriggers) tagTriggers.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            else if (id == R.id.switchPEF) tagPEF.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            else if (id == R.id.switchTriage) tagTriage.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            else if (id == R.id.switchCharts) tagCharts.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            if (!isLoading) {
                textSaving.setVisibility(View.VISIBLE);
                textSaving.setText("Saving...");

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

        docRef.set(data, SetOptions.merge())
                .addOnSuccessListener(a -> {
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
