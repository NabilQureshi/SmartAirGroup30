package com.example.smartair.checkin;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class SymptomCheckInActivity extends AppCompatActivity {

    // UI
    private RadioGroup rgNightWaking, rgActivityLimits, rgCough, rgChestPain;
    private Chip chipExercise, chipColdAir, chipDustPets, chipSmoke, chipIllness, chipPerfume;
    private Button btnSubmit;

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Submission Identity
    private boolean parentMode = false;
    private String loggedInUid = "anonymous";  // whoever is using device
    private String targetUid = "anonymous";    // whose record is being written
    private String submittedByRole = "guest";  // data written to DB

    private String todayId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_check_in);

        todayId = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // ------------------------ WHO IS SUBMITTING? ------------------------
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loggedInUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        parentMode = getIntent().getBooleanExtra("openedByParent", false);

        if (parentMode) {
            targetUid = getIntent().getStringExtra("childId");
            submittedByRole = "parent";
            Toast.makeText(this, "Parent editing child's check-in", Toast.LENGTH_SHORT).show();

        } else {
            targetUid = loggedInUid; // child is submitting for themselves
            submittedByRole = "child";
        }

        // ------------------------ UI SETUP ------------------------
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

        setupRadioListeners();
        btnSubmit.setOnClickListener(v -> saveCheckin());
    }

    private void setupRadioListeners() {
        RadioGroup.OnCheckedChangeListener listener = (g, id) -> validateSelectable();
        rgNightWaking.setOnCheckedChangeListener(listener);
        rgActivityLimits.setOnCheckedChangeListener(listener);
        rgCough.setOnCheckedChangeListener(listener);
        rgChestPain.setOnCheckedChangeListener(listener);
    }

    private void validateSelectable() {
        boolean enable = rgNightWaking.getCheckedRadioButtonId() != -1 &&
                rgActivityLimits.getCheckedRadioButtonId() != -1 &&
                rgCough.getCheckedRadioButtonId() != -1 &&
                rgChestPain.getCheckedRadioButtonId() != -1;
        btnSubmit.setEnabled(enable);
    }

    private String sel(RadioGroup g){
        return ((RadioButton)findViewById(g.getCheckedRadioButtonId())).getText().toString();
    }

    private void saveCheckin() {

        List<String> triggers = new ArrayList<>();
        if (chipExercise.isChecked()) triggers.add("Exercise");
        if (chipColdAir.isChecked()) triggers.add("Cold Air");
        if (chipDustPets.isChecked()) triggers.add("Dust/Pets");
        if (chipSmoke.isChecked()) triggers.add("Smoke");
        if (chipIllness.isChecked()) triggers.add("Illness");
        if (chipPerfume.isChecked()) triggers.add("Perfume/Odors");

        Map<String,Object> data = new HashMap<>();
        data.put("nightWaking", sel(rgNightWaking));
        data.put("activityLimits", sel(rgActivityLimits));
        data.put("cough", sel(rgCough));
        data.put("chestPain", sel(rgChestPain));
        data.put("triggers", triggers);
        data.put("submittedBy", submittedByRole);   // child or parent
        data.put("submittedByUid", loggedInUid);    // who actually pressed submit
        data.put("lastUpdated", System.currentTimeMillis());

        DocumentReference doc = db.collection("symptomCheckIns")
                .document(targetUid)    // ALWAYS child's UID
                .collection("daily")
                .document(todayId);

        doc.set(data)
                .addOnSuccessListener(a->{
                    Toast.makeText(this,"Saved for "+todayId,Toast.LENGTH_SHORT).show();
                    finish(); // return to home page
                })
                .addOnFailureListener(e->Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show());
    }
}
