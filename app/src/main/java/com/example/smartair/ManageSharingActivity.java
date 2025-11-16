package com.example.smartair;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
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
    private Button btnSave;

    private String parentId, childId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sharing);

        db = FirebaseFirestore.getInstance();
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        childId = getIntent().getStringExtra("childId");

        // Bind switches
        switchRescue = findViewById(R.id.switchRescueLogs);
        switchController = findViewById(R.id.switchController);
        switchSymptoms = findViewById(R.id.switchSymptoms);
        switchTriggers = findViewById(R.id.switchTriggers);
        switchPEF = findViewById(R.id.switchPEF);
        switchTriage = findViewById(R.id.switchTriage);
        switchCharts = findViewById(R.id.switchCharts);

        btnSave = findViewById(R.id.btnSaveSharing);

        loadExistingSettings();
        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void loadExistingSettings() {
        DocumentReference ref = db.collection("parents")
                .document(parentId)
                .collection("children")
                .document(childId)
                .collection("settings")
                .document("sharing");

        ref.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                switchRescue.setChecked(Boolean.TRUE.equals(doc.getBoolean("rescueLogs")));
                switchController.setChecked(Boolean.TRUE.equals(doc.getBoolean("controller")));
                switchSymptoms.setChecked(Boolean.TRUE.equals(doc.getBoolean("symptoms")));
                switchTriggers.setChecked(Boolean.TRUE.equals(doc.getBoolean("triggers")));
                switchPEF.setChecked(Boolean.TRUE.equals(doc.getBoolean("pef")));
                switchTriage.setChecked(Boolean.TRUE.equals(doc.getBoolean("triage")));
                switchCharts.setChecked(Boolean.TRUE.equals(doc.getBoolean("charts")));
            }
        });
    }

    private void saveSettings() {
        Map<String, Object> data = new HashMap<>();
        data.put("rescueLogs", switchRescue.isChecked());
        data.put("controller", switchController.isChecked());
        data.put("symptoms", switchSymptoms.isChecked());
        data.put("triggers", switchTriggers.isChecked());
        data.put("pef", switchPEF.isChecked());
        data.put("triage", switchTriage.isChecked());
        data.put("charts", switchCharts.isChecked());

        db.collection("parents")
                .document(parentId)
                .collection("children")
                .document(childId)
                .collection("settings")
                .document("sharing")
                .set(data)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Sharing settings updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
