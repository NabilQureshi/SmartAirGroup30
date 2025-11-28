package com.example.smartair;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;
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

    // User / Parent Mode
    private String userRole = "Guest";
    private String userId = "anonymous";      // logged-in user (child if self)
    private String targetUserId = "anonymous"; // who we are saving the check-in under
    private boolean parentMode = false;

    private String todayId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_check_in);

        db = FirebaseFirestore.getInstance();
        todayId = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Detect if parent opened this for a child
        parentMode = getIntent().getBooleanExtra("openedByParent", false);

        // If parent editing a child
        if (parentMode) {
            targetUserId = getIntent().getStringExtra("childId");
            userRole = "parent"; // display and store who performed the check-in
            Toast.makeText(this, "Editing symptom check-in for Child", Toast.LENGTH_SHORT).show();
        }

        // If child is logged in normally
        else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            targetUserId = userId; // child can only edit themselves
            fetchUserRole();       // load role but still save to own uid
        } else {
            Toast.makeText(this, "Not logged in — saving as Guest", Toast.LENGTH_SHORT).show();
        }

        // UI Bindings
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

    // Get role from users/{uid}/role  (only used for children self check-ins)
    private void fetchUserRole() {
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.getString("role") != null) {
                userRole = doc.getString("role");
            }
        });
    }

    private void setupRadioGroupListeners() {
        RadioGroup.OnCheckedChangeListener listener = (group, checkedId) -> checkAllSelected();
        rgNightWaking.setOnCheckedChangeListener(listener);
        rgActivityLimits.setOnCheckedChangeListener(listener);
        rgCough.setOnCheckedChangeListener(listener);
        rgChestPain.setOnCheckedChangeListener(listener);
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

        Map<String, Object> data = new HashMap<>();
        data.put("nightWaking", getSelected(rgNightWaking));
        data.put("activityLimits", getSelected(rgActivityLimits));
        data.put("cough", getSelected(rgCough));
        data.put("chestPain", getSelected(rgChestPain));
        data.put("triggers", triggers);
        data.put("submittedBy", userRole);        // <-- CHILD or PARENT
        data.put("lastUpdated", System.currentTimeMillis());

        DocumentReference doc = db.collection("symptomCheckIns")
                .document(targetUserId)          // <-- CHILD UID or SELF UID
                .collection("daily")
                .document(todayId);              // one per day

        doc.set(data).addOnSuccessListener(x -> {
            Toast.makeText(this, "Saved for: " + todayId, Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }
}