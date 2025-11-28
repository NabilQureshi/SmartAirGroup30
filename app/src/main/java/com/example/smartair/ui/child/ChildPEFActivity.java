package com.example.smartair.ui.child;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.model.PEFEntry;
import com.example.smartair.util.PEFValidator;
import com.example.smartair.util.PEFZoneCalculator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

/**
 * Child activity for entering PEF values with pre/post-medicine tags.
 * Uses Firebase Firestore for data storage instead of local SharedPreferences.
 */
public class ChildPEFActivity extends AppCompatActivity {
    private EditText pefInput;
    private TextView pefWarningText;
    private RadioGroup medicineTagGroup;
    private RadioButton preMedicineRadio;
    private RadioButton postMedicineRadio;
    private RadioButton noneRadio;
    private MaterialButton saveButton;
    private RecyclerView pefHistoryRecyclerView;
    private PEFHistoryAdapter historyAdapter;
    private TextView emptyStateText;
    private TextView personalBestValueText;
    private TextView personalBestStatusText;
    private MaterialCardView zoneCard;
    private View zoneContentGroup;
    private TextView zoneStateText;
    private TextView zonePercentText;
    private TextView zoneGuidanceText;
    private TextView zoneSourceText;
    private TextView zoneEmptyText;

    private Integer latestSavedPef;
    private Integer currentPersonalBest;

    // Firebase
    private FirebaseFirestore db;
    private CollectionReference pefCollection;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_pef);

        // Firebase setup
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            pefCollection = db.collection("users")
                    .document(user.getUid())
                    .collection("pef_entries");
        }

        // Views
        pefInput = findViewById(R.id.pef_input);
        pefWarningText = findViewById(R.id.pef_warning_text);
        medicineTagGroup = findViewById(R.id.medicine_tag_group);
        preMedicineRadio = findViewById(R.id.pre_medicine_radio);
        postMedicineRadio = findViewById(R.id.post_medicine_radio);
        noneRadio = findViewById(R.id.none_radio);
        saveButton = findViewById(R.id.save_pef_button);
        pefHistoryRecyclerView = findViewById(R.id.pef_history_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        personalBestValueText = findViewById(R.id.personal_best_value);
        personalBestStatusText = findViewById(R.id.personal_best_status_text);
        zoneCard = findViewById(R.id.zone_card);
        zoneContentGroup = findViewById(R.id.zone_content_group);
        zoneStateText = findViewById(R.id.zone_state_text);
        zonePercentText = findViewById(R.id.zone_percent_text);
        zoneGuidanceText = findViewById(R.id.zone_guidance_text);
        zoneSourceText = findViewById(R.id.zone_source_text);
        zoneEmptyText = findViewById(R.id.zone_empty_text);

        pefInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        pefInput.setHint(R.string.pef_input_hint);

        // Realtime validation
        pefInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePEFInput(s.toString());
                updateZoneCardUsingCurrentState();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        noneRadio.setChecked(true);

        pefHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new PEFHistoryAdapter();
        pefHistoryRecyclerView.setAdapter(historyAdapter);

        saveButton.setOnClickListener(v -> savePEFEntry());

        MaterialButton triageButton = findViewById(R.id.triage_button);
        if (triageButton != null) {
            triageButton.setOnClickListener(v ->
                    startActivity(new Intent(this, ChildTriageActivity.class))
            );
        }

        updatePersonalBestCard();
        fetchPEFEntries();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePersonalBestCard();
        fetchPEFEntries();
    }

    private void validatePEFInput(String pefString) {
        if (pefString == null || pefString.trim().isEmpty()) {
            pefWarningText.setVisibility(View.GONE);
            return;
        }
        try {
            int pefValue = Integer.parseInt(pefString.trim());
            PEFValidator.ValidationResult result = PEFValidator.validatePEF(pefValue);

            if (result.hasWarning()) {
                pefWarningText.setText(result.getWarningMessage());
                pefWarningText.setVisibility(View.VISIBLE);
                pefWarningText.setTextColor(result.isValid()
                        ? ContextCompat.getColor(this, android.R.color.holo_orange_dark)
                        : ContextCompat.getColor(this, android.R.color.holo_red_dark));
            } else {
                pefWarningText.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            pefWarningText.setVisibility(View.GONE);
        }
    }

    private void savePEFEntry() {
        String pefString = pefInput.getText().toString().trim();

        if (TextUtils.isEmpty(pefString)) {
            Toast.makeText(this, "Please enter a PEF value", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int pefValue = Integer.parseInt(pefString);
            PEFValidator.ValidationResult result = PEFValidator.validatePEF(pefValue);
            if (!result.isValid()) {
                Toast.makeText(this, result.getWarningMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            PEFEntry.MedicineTag tag = PEFEntry.MedicineTag.NONE;
            int selectedId = medicineTagGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.pre_medicine_radio) tag = PEFEntry.MedicineTag.PRE_MEDICINE;
            else if (selectedId == R.id.post_medicine_radio) tag = PEFEntry.MedicineTag.POST_MEDICINE;

            long timestamp = System.currentTimeMillis();
            Map<String, Object> data = new HashMap<>();
            data.put("pefValue", pefValue);
            data.put("timestamp", timestamp);
            data.put("medicineTag", tag.name());

            saveButton.setEnabled(false);
            pefCollection.add(data)
                    .addOnSuccessListener(docRef -> {
                        saveButton.setEnabled(true);
                        Toast.makeText(this, "PEF entry saved!", Toast.LENGTH_SHORT).show();
                        pefInput.setText("");
                        noneRadio.setChecked(true);
                        fetchPEFEntries();
                    })
                    .addOnFailureListener(e -> {
                        saveButton.setEnabled(true);
                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPEFEntries() {
        if (pefCollection == null) return;

        pefCollection.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener((QuerySnapshot querySnapshot) -> {
                    List<PEFEntry> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        int value = doc.getLong("pefValue").intValue();
                        long timestamp = doc.getLong("timestamp");
                        String tagStr = doc.getString("medicineTag");
                        PEFEntry.MedicineTag tag = PEFEntry.MedicineTag.NONE;
                        if (tagStr != null) tag = PEFEntry.MedicineTag.valueOf(tagStr);

                        entries.add(new PEFEntry(value, timestamp, tag));
                    }

                    historyAdapter.setPEFEntries(entries);
                    latestSavedPef = entries.isEmpty() ? null : entries.get(0).getPefValue();
                    updateZoneCardUsingCurrentState();

                    emptyStateText.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
                    pefHistoryRecyclerView.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch entries: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePersonalBestCard() {
        // 这里仍可使用 SharedPreferences 或 Firebase PB，暂不改动
        if (personalBestValueText == null || personalBestStatusText == null) return;

        if (currentPersonalBest != null) {
            personalBestValueText.setText(getString(R.string.personal_best_value_format, currentPersonalBest));
            personalBestStatusText.setText(R.string.personal_best_status_set);
        } else {
            personalBestValueText.setText(R.string.personal_best_status_missing);
            personalBestStatusText.setText(R.string.personal_best_description);
        }
        updateZoneCardUsingCurrentState();
    }

    private void updateZoneCardUsingCurrentState() {
        if (zoneCard == null) return;
        Integer inputValue = parseInputValue();
        if (inputValue != null) updateZoneCard(inputValue, true);
        else updateZoneCard(latestSavedPef, false);
    }

    private Integer parseInputValue() {
        if (pefInput == null) return null;
        String pefString = pefInput.getText().toString().trim();
        if (pefString.isEmpty()) return null;
        try {
            int value = Integer.parseInt(pefString);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void updateZoneCard(Integer pefValue, boolean basedOnInput) {
        if (currentPersonalBest == null || currentPersonalBest <= 0) {
            zoneContentGroup.setVisibility(View.GONE);
            zoneEmptyText.setVisibility(View.VISIBLE);
            zoneEmptyText.setText(R.string.zone_missing_pb);
            applyZoneColors(null);
            return;
        }
        if (pefValue == null) {
            zoneContentGroup.setVisibility(View.GONE);
            zoneEmptyText.setVisibility(View.VISIBLE);
            zoneEmptyText.setText(R.string.zone_missing_entries);
            applyZoneColors(null);
            return;
        }

        PEFZoneCalculator.ZoneResult result = PEFZoneCalculator.calculateZone(pefValue, currentPersonalBest);
        if (!result.isReady() || result.getZone() == PEFZoneCalculator.Zone.UNKNOWN) {
            zoneContentGroup.setVisibility(View.GONE);
            zoneEmptyText.setVisibility(View.VISIBLE);
            zoneEmptyText.setText(R.string.zone_unknown_guidance);
            applyZoneColors(null);
            return;
        }

        zoneContentGroup.setVisibility(View.VISIBLE);
        zoneEmptyText.setVisibility(View.GONE);

        int labelRes, guidanceRes;
        switch (result.getZone()) {
            case GREEN:
                labelRes = R.string.zone_label_green;
                guidanceRes = R.string.zone_green_guidance;
                break;
            case YELLOW:
                labelRes = R.string.zone_label_yellow;
                guidanceRes = R.string.zone_yellow_guidance;
                break;
            case RED:
            default:
                labelRes = R.string.zone_label_red;
                guidanceRes = R.string.zone_red_guidance;
                break;
        }

        zoneStateText.setText(labelRes);
        zoneGuidanceText.setText(guidanceRes);
        zonePercentText.setText(getString(R.string.zone_percent_format,
                formatPercent(result.getPercentOfPersonalBest())));
        zoneSourceText.setText(basedOnInput ? R.string.zone_based_on_current_entry
                : R.string.zone_based_on_latest_entry);

        applyZoneColors(result.getZone());
        if (!basedOnInput) {
            persistLatestZone(result);
        }
    }

    private String formatPercent(float percent) {
        if (percent < 0f) percent = 0f;
        return String.format(Locale.getDefault(), "%.0f", percent);
    }

    private void applyZoneColors(PEFZoneCalculator.Zone zone) {
        int backgroundColorRes, textColorRes;

        if (zone == null) {
            backgroundColorRes = R.color.zone_neutral_bg;
            textColorRes = android.R.color.black;
        } else {
            switch (zone) {
                case GREEN:
                    backgroundColorRes = R.color.zone_green_bg;
                    textColorRes = R.color.zone_green_text;
                    break;
                case YELLOW:
                    backgroundColorRes = R.color.zone_yellow_bg;
                    textColorRes = R.color.zone_yellow_text;
                    break;
                case RED:
                default:
                    backgroundColorRes = R.color.zone_red_bg;
                    textColorRes = R.color.zone_red_text;
                    break;
            }
        }

        zoneCard.setCardBackgroundColor(ContextCompat.getColor(this, backgroundColorRes));
        zoneStateText.setTextColor(ContextCompat.getColor(this, textColorRes));
        zonePercentText.setTextColor(ContextCompat.getColor(this, textColorRes));
    }

    /**
     * Saves the latest calculated zone for the child so the parent app can display it.
     * Only persists when the zone is based on the latest saved entry to avoid noisy writes.
     */
    private void persistLatestZone(PEFZoneCalculator.ZoneResult result) {
        if (result == null || !result.isReady() || user == null) return;

        Map<String, Object> update = new HashMap<>();
        update.put("latestZoneState", result.getZone().name());
        update.put("latestZonePercent", result.getPercentOfPersonalBest());
        update.put("latestZonePefValue", result.getPefValue());
        update.put("latestZoneUpdatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(user.getUid())
                .set(update, SetOptions.merge());
    }
}
