package com.example.smartair.checkin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.example.smartair.R;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.text.SimpleDateFormat;
import java.util.*;

public class SymptomCheckInActivity extends AppCompatActivity {

    // UI
    private RadioGroup rgNightWaking, rgActivityLimits, rgCough, rgChestPain;
    private Chip chipExercise, chipColdAir, chipDustPets, chipSmoke, chipIllness, chipPerfume;
    private Button btnSubmit;

    // Firebase
    private FirebaseFirestore db;

    // Roles & user flow
    private String submittedBy = "Guest";   // who is performing check-in
    private String targetUID = "anonymous"; // who the check-in belongs to (child)
    private boolean openedByParent = false;

    private String todayId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_check_in);

        db = FirebaseFirestore.getInstance();
        todayId = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Determine if parent opened a child check-in
        openedByParent = getIntent().getBooleanExtra("openedByParent", false);
        String childId = getIntent().getStringExtra("childId");

        if (openedByParent && childId != null) {
            targetUID = childId;
            submittedBy = "parent";   // parent is recording FOR child
            Toast.makeText(this, "Parent editing child's check-in", Toast.LENGTH_SHORT).show();
        }
        else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            targetUID = FirebaseAuth.getInstance().getCurrentUser().getUid(); // save under child self account
            submittedBy = "child";  // child doing their own check-in
        }
        else {
            targetUID = "anonymous";
            submittedBy = "Guest"; // emergency use only
            Toast.makeText(this, "No login detected — Guest mode", Toast.LENGTH_SHORT).show();
        }

        // UI binding
        rgNightWaking = findViewById(R.id.rg_night_waking);
        rgActivityLimits = findViewById(R.id.rg_activity_limits);
        rgCough = findViewById(R.id.rg_cough);
        rgChestPain = findViewById(R.id.rg_chest_pain);

        chipExercise = findViewById(R.id.trigger1);
        chipColdAir = findViewById(R.id.trigger2);
        chipDustPets = findViewById(R.id.trigger3);
        chipSmoke = findViewById(R.id.trigger4);
        chipIllness = findViewById(R.id.trigger5);
        chipPerfume = findViewById(R.id.trigger6);

        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setEnabled(false);

        setupRadioGroupListeners();
        btnSubmit.setOnClickListener(v -> saveOrUpdateCheckIn());
    }

    private void setupRadioGroupListeners() {
        RadioGroup.OnCheckedChangeListener listen = (group, checkedId) -> checkAllSelected();
        rgNightWaking.setOnCheckedChangeListener(listen);
        rgActivityLimits.setOnCheckedChangeListener(listen);
        rgCough.setOnCheckedChangeListener(listen);
        rgChestPain.setOnCheckedChangeListener(listen);
    }

    private void checkAllSelected() {
        boolean ready =
                rgNightWaking.getCheckedRadioButtonId() != -1 &&
                        rgActivityLimits.getCheckedRadioButtonId() != -1 &&
                        rgCough.getCheckedRadioButtonId() != -1 &&
                        rgChestPain.getCheckedRadioButtonId() != -1;

        btnSubmit.setEnabled(ready);
    }

    private String getSelected(RadioGroup g) {
        RadioButton rb = findViewById(g.getCheckedRadioButtonId());
        return rb.getText().toString();
    }

    private void saveOrUpdateCheckIn() {

        List<String> triggers = new ArrayList<>();
        if (chipExercise.isChecked()) triggers.add("Exercise");
        if (chipColdAir.isChecked()) triggers.add("Cold Air");
        if (chipDustPets.isChecked()) triggers.add("Dust/Pets");
        if (chipSmoke.isChecked()) triggers.add("Smoke");
        if (chipIllness.isChecked()) triggers.add("Illness");
        if (chipPerfume.isChecked()) triggers.add("Perfume/Odors");

        Map<String,Object> data = new HashMap<>();
        data.put("nightWaking", getSelected(rgNightWaking));
        data.put("activityLimits", getSelected(rgActivityLimits));
        data.put("cough", getSelected(rgCough));
        data.put("chestPain", getSelected(rgChestPain));
        data.put("triggers", triggers);
        data.put("submittedBy", submittedBy);  // <-- FIXED always correct
        data.put("lastUpdated", System.currentTimeMillis());

        DocumentReference doc = db.collection("symptomCheckIns")
                .document(targetUID)
                .collection("daily")
                .document(todayId); // only 1 per day per user

        doc.set(data)
                .addOnSuccessListener(x -> {
                    Toast.makeText(this, "Saved for " + todayId + " (" + submittedBy + ")", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
