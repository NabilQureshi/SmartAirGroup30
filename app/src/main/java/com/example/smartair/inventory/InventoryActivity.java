package com.example.smartair.inventory;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    private EditText etDate, etDose;
    private Spinner spType;
    private Button btnSubmitInventory;
    private RecyclerView rvInventoryHistory;
    private InventoryLogAdapter adapter;
    private TextView emptyStateText;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private CollectionReference inventoryRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        etDate = findViewById(R.id.etDate);
        etDose = findViewById(R.id.etDose);
        spType = findViewById(R.id.spType);
        btnSubmitInventory = findViewById(R.id.btnSubmitInventory);
        rvInventoryHistory = findViewById(R.id.rvInventoryHistory);
        emptyStateText = findViewById(R.id.empty_state_text);

        // 初始化 Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inventoryRef = db.collection("users").document(user.getUid()).collection("inventory");

        // 设置 RecyclerView
        rvInventoryHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryLogAdapter();
        rvInventoryHistory.setAdapter(adapter);

        // 设置 Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Rescue", "Controller"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(spinnerAdapter);

        // 点击提交
        btnSubmitInventory.setOnClickListener(v -> submitInventory());

        // 加载历史记录
        fetchInventoryHistory();

        // 新增：库存检查和过期提醒
        checkInventoryAndExpiry();
    }

    private void submitInventory() {
        String dateStr = etDate.getText().toString();
        if (TextUtils.isEmpty(dateStr)) {
            Toast.makeText(this, "Please enter purchase date", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = spType.getSelectedItem() != null ? spType.getSelectedItem().toString() : null;
        if (TextUtils.isEmpty(type)) {
            Toast.makeText(this, "Please select a type", Toast.LENGTH_SHORT).show();
            return;
        }

        String doseStr = etDose.getText().toString();
        if (TextUtils.isEmpty(doseStr)) {
            Toast.makeText(this, "Please enter dose (puffs)", Toast.LENGTH_SHORT).show();
            return;
        }

        long dose;
        try {
            dose = Long.parseLong(doseStr);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Dose must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("email", user.getEmail());
        data.put("date", dateStr);
        data.put("type", type);
        data.put("dose", dose);

        btnSubmitInventory.setEnabled(false);
        inventoryRef.add(data).addOnSuccessListener(docRef -> {
            btnSubmitInventory.setEnabled(true);
            Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show();
            etDate.setText("");
            etDose.setText("");
            fetchInventoryHistory();
        }).addOnFailureListener(e -> {
            btnSubmitInventory.setEnabled(true);
            Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchInventoryHistory() {
        inventoryRef.orderBy("date").limit(50).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> records = queryDocumentSnapshots.getDocuments();
            if (records.isEmpty()) {
                emptyStateText.setVisibility(View.VISIBLE);
                rvInventoryHistory.setVisibility(View.GONE);
            } else {
                emptyStateText.setVisibility(View.GONE);
                rvInventoryHistory.setVisibility(View.VISIBLE);
                adapter.setInventory(records);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 检查 inventory 的库存是否足够，以及是否有药品快过期
     */
    private void checkInventoryAndExpiry() {
        long now = System.currentTimeMillis();
        long oneYearMillis = 365L * 24 * 60 * 60 * 1000;

        // 1. 获取 parent inventory
        inventoryRef.get().addOnSuccessListener(parentSnapshot -> {
            HashMap<String, Long> inventoryCount = new HashMap<>();
            boolean hasExpiring = false;

            for (DocumentSnapshot doc : parentSnapshot.getDocuments()) {
                String type = doc.getString("type");
                Long dose = doc.getLong("dose");
                String dateStr = doc.getString("date");

                if (type == null || dose == null || dateStr == null) continue;

                // 简单解析日期为时间戳
                long purchaseTime = parseDateToMillis(dateStr);
                if (purchaseTime + oneYearMillis - now <= 7 * 24 * 60 * 60 * 1000) {
                    hasExpiring = true; // 7天内快过期
                }

                inventoryCount.put(type, inventoryCount.getOrDefault(type, 0L) + dose);
            }

            // 2. 获取所有 children 的 uid
            boolean finalHasExpiring = hasExpiring;
            db.collection("users").document(user.getUid()).collection("children")
                    .get().addOnSuccessListener(childrenSnapshot -> {
                        List<String> childrenUids = new ArrayList<>();
                        for (DocumentSnapshot childDoc : childrenSnapshot.getDocuments()) {
                            childrenUids.add(childDoc.getId());
                        }

                        // 3. 遍历 children medicine_logs，统计消耗量
                        if (childrenUids.isEmpty()) {
                            // 没有孩子，不需要弹窗库存不足
                            if (finalHasExpiring) showExpiringAlert();
                            return;
                        }

                        HashMap<String, Long> childrenUsage = new HashMap<>();
                        final int[] processed = {0};

                        for (String childUid : childrenUids) {
                            db.collection("users").document(childUid).collection("medicine_logs")
                                    .get().addOnSuccessListener(logsSnapshot -> {
                                        for (DocumentSnapshot log : logsSnapshot.getDocuments()) {
                                            String type = log.getString("type");
                                            Long dose = log.getLong("dose");
                                            if (type != null && dose != null) {
                                                childrenUsage.put(type, childrenUsage.getOrDefault(type, 0L) + dose);
                                            }
                                        }
                                        processed[0]++;
                                        if (processed[0] == childrenUids.size()) {
                                            // 遍历完所有孩子，开始判断库存是否足够
                                            boolean lowInventory = false;
                                            for (String type : childrenUsage.keySet()) {
                                                long required = childrenUsage.get(type);
                                                long available = inventoryCount.getOrDefault(type, 0L);
                                                if (available < required) {
                                                    lowInventory = true;
                                                    break;
                                                }
                                            }

                                            if (finalHasExpiring) showExpiringAlert();
                                            if (lowInventory) showLowInventoryAlert();
                                        }
                                    });
                        }
                    });

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to check inventory: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // 解析日期字符串为时间戳（示例 yyyy-MM-dd）
    private long parseDateToMillis(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateStr).getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    // 弹窗提醒药品快过期
    private void showExpiringAlert() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Medicine Expiry Alert")
                .setMessage("Some medicines are about to expire within a week. Please purchase them in time.")
                .setPositiveButton("OK", null)
                .show();
    }

    // 弹窗提醒库存不足
    private void showLowInventoryAlert() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Low Inventory Alert")
                .setMessage("Your inventory may not be enough for your children's medicine usage. Please top up.")
                .setPositiveButton("OK", null)
                .show();
    }
}
