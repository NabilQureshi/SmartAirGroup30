package com.example.smartair.ui.child;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartair.R;
import com.example.smartair.data.PEFRepository;
import com.example.smartair.model.PEFEntry;
import com.google.android.material.button.MaterialButton;
import java.util.List;

/**
 * Child activity for entering PEF values with pre/post-medicine tags.
 * Allows children to see how medicine affects their breathing.
 */
public class ChildPEFActivity extends AppCompatActivity {
    private PEFRepository pefRepository;
    private EditText pefInput;
    private RadioGroup medicineTagGroup;
    private RadioButton preMedicineRadio;
    private RadioButton postMedicineRadio;
    private RadioButton noneRadio;
    private MaterialButton saveButton;
    private RecyclerView pefHistoryRecyclerView;
    private PEFHistoryAdapter historyAdapter;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_pef);

        pefRepository = new PEFRepository(this);

        pefInput = findViewById(R.id.pef_input);
        medicineTagGroup = findViewById(R.id.medicine_tag_group);
        preMedicineRadio = findViewById(R.id.pre_medicine_radio);
        postMedicineRadio = findViewById(R.id.post_medicine_radio);
        noneRadio = findViewById(R.id.none_radio);
        saveButton = findViewById(R.id.save_pef_button);
        pefHistoryRecyclerView = findViewById(R.id.pef_history_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);

        pefInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        pefInput.setHint("Enter PEF value (L/min)");

        // Set default to none
        noneRadio.setChecked(true);

        pefHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new PEFHistoryAdapter();
        pefHistoryRecyclerView.setAdapter(historyAdapter);

        saveButton.setOnClickListener(v -> savePEFEntry());

        refreshHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHistory();
    }

    private void savePEFEntry() {
        String pefString = pefInput.getText().toString().trim();
        
        if (pefString.isEmpty()) {
            Toast.makeText(this, "Please enter a PEF value", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int pefValue = Integer.parseInt(pefString);
            
            if (pefValue <= 0) {
                Toast.makeText(this, "PEF value must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pefValue > 1000) {
                Toast.makeText(this, "Please enter a realistic value (typically 100-800 L/min)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected medicine tag
            PEFEntry.MedicineTag tag = PEFEntry.MedicineTag.NONE;
            int selectedId = medicineTagGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.pre_medicine_radio) {
                tag = PEFEntry.MedicineTag.PRE_MEDICINE;
            } else if (selectedId == R.id.post_medicine_radio) {
                tag = PEFEntry.MedicineTag.POST_MEDICINE;
            }

            long timestamp = System.currentTimeMillis();
            PEFEntry entry = new PEFEntry(pefValue, timestamp, tag);
            pefRepository.addPEFEntry(entry);

            Toast.makeText(this, "PEF entry saved!", Toast.LENGTH_SHORT).show();

            // Clear input
            pefInput.setText("");
            noneRadio.setChecked(true);

            refreshHistory();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshHistory() {
        List<PEFEntry> entries = pefRepository.getAllPEFEntries();
        historyAdapter.setPEFEntries(entries);

        if (entries.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            pefHistoryRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            pefHistoryRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}

