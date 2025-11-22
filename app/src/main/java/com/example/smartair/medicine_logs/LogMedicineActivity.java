package com.example.smartair.medicine_logs;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LogMedicineActivity extends AppCompatActivity {

    private RadioGroup rgType;
    private TextInputEditText etDose;
    private MaterialButton btnSubmit;
    private RecyclerView rvHistory;
    private MedicineLogAdapter adapter;
    private View emptyStateText;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference logRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_medicine);

        rgType = findViewById(R.id.rgType);
        etDose = findViewById(R.id.etDose);
        btnSubmit = findViewById(R.id.btnSubmitLog);
        rvHistory = findViewById(R.id.rvLogHistory);
        emptyStateText = findViewById(R.id.empty_state_text);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        logRef = db.collection("users").document(user.getUid()).collection("medicine_logs");

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineLogAdapter();
        rvHistory.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> submitLog());

        fetchLogs();
    }

    private void submitLog() {
        int checkedId = rgType.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "请选择 Rescue 或 Controller", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rb = findViewById(checkedId);
        String type = rb.getText().toString();

        String doseStr = etDose.getText().toString();
        if (TextUtils.isEmpty(doseStr)) {
            Toast.makeText(this, "请输入剂量（puffs）", Toast.LENGTH_SHORT).show();
            return;
        }

        long dose;
        try {
            dose = Long.parseLong(doseStr);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "剂量必须为数字", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        HashMap<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("email", user.getEmail());
        data.put("type", type);
        data.put("dose", dose);
        data.put("timestamp", now);

        btnSubmit.setEnabled(false);
        logRef.add(data).addOnSuccessListener(documentReference -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(this, "记录已保存", Toast.LENGTH_SHORT).show();
            etDose.setText("");
            fetchLogs(); // 更新列表
        }).addOnFailureListener(e -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchLogs() {
        logRef.orderBy("timestamp").limit(50).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> logs = queryDocumentSnapshots.getDocuments();
            if (logs.isEmpty()) {
                emptyStateText.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            } else {
                emptyStateText.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);
                adapter.setLogs(logs);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "读取历史失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
