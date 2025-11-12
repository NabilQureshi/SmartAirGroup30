package com.example.smartair;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class LogMedicineActivity extends AppCompatActivity {

    private RadioGroup rgType;
    private EditText etDose;
    private Button btnSubmit;
    private ListView lvHistory;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference logRef;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listItems = new ArrayList<>();

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_medicine);

        rgType = findViewById(R.id.rgType);
        etDose = findViewById(R.id.etDose);
        btnSubmit = findViewById(R.id.btnSubmitLog);
        lvHistory = findViewById(R.id.lvLogHistory);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        logRef = db.collection("users").document(user.getUid()).collection("medicine_logs");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        lvHistory.setAdapter(adapter);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitLog();
            }
        });

        // 读取现有记录（简单实现：按创建时间降序读取最近 50 条）
        logRef.orderBy("timestamp").limit(50).get().addOnSuccessListener(queryDocumentSnapshots -> {
            listItems.clear();
            for (var doc : queryDocumentSnapshots.getDocuments()) {
                String type = doc.getString("type");
                Long ts = doc.getLong("timestamp");
                Long dose = doc.getLong("dose");
                String time = ts == null ? "" : sdf.format(new Date(ts));
                listItems.add(type + " | dose: " + (dose==null? "-" : dose) + " | " + time);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(LogMedicineActivity.this, "读取历史失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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
        data.put("type", type);
        data.put("dose", dose);
        data.put("timestamp", now);

        btnSubmit.setEnabled(false);
        logRef.add(data).addOnSuccessListener(documentReference -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(LogMedicineActivity.this, "记录已保存", Toast.LENGTH_SHORT).show();
            String display = type + " | dose: " + dose + " | " + sdf.format(new Date(now));
            listItems.add(0, display); // add to top
            adapter.notifyDataSetChanged();
            etDose.setText("");
        }).addOnFailureListener(e -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(LogMedicineActivity.this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
