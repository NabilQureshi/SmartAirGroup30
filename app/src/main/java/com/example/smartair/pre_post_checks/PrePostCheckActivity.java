package com.example.smartair.pre_post_checks;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.badges_system.BadgeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PrePostCheckActivity extends AppCompatActivity {

    private RadioGroup rgWhen;
    private RadioGroup rgResult;
    private RatingBar ratingBar;
    private EditText etNote;
    private Button btnSubmit;
    private RecyclerView rvCheckHistory;
    private PrePostCheckAdapter adapter;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference checkRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_post_check);

        rgWhen = findViewById(R.id.rgWhen);
        rgResult = findViewById(R.id.rgResult);
        ratingBar = findViewById(R.id.ratingBar);
        etNote = findViewById(R.id.etNote);
        btnSubmit = findViewById(R.id.btnSubmitCheck);
        rvCheckHistory = findViewById(R.id.rvCheckHistory);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkRef = db.collection("users")
                .document(user.getUid())
                .collection("prepost_checks");

        // 初始化 RecyclerView
        adapter = new PrePostCheckAdapter();
        rvCheckHistory.setAdapter(adapter);
        rvCheckHistory.setLayoutManager(new LinearLayoutManager(this));

        // 读取历史记录
        checkRef.orderBy("timestamp").limit(50).get()
                .addOnSuccessListener(queryDocumentSnapshots -> adapter.setChecks(queryDocumentSnapshots.getDocuments()))
                .addOnFailureListener(e -> Toast.makeText(this, "读取历史失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        btnSubmit.setOnClickListener(view -> submitCheck());
    }

    private void submitCheck() {
        int whenId = rgWhen.getCheckedRadioButtonId();
        int resId = rgResult.getCheckedRadioButtonId();
        if (whenId == -1 || resId == -1) {
            Toast.makeText(this, "请选择 Before/After 和 Better/Same/Worse", Toast.LENGTH_SHORT).show();
            return;
        }

        String when = ((RadioButton) findViewById(whenId)).getText().toString();
        String result = ((RadioButton) findViewById(resId)).getText().toString();
        float rating = ratingBar.getRating();
        String note = etNote.getText().toString();

        String uid = user.getUid();
        String email = user.getEmail();
        long now = System.currentTimeMillis();

        HashMap<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("email", email);
        data.put("when", when);
        data.put("result", result);
        data.put("rating", rating);
        data.put("note", note);
        data.put("timestamp", now);

        btnSubmit.setEnabled(false);
        checkRef.add(data).addOnSuccessListener(documentReference -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(this, "评估已保存", Toast.LENGTH_SHORT).show();

            // 自动检查徽章
            checkAndUnlockBadge(uid);

            // 将刚提交的数据直接插入 RecyclerView
            documentReference.get().addOnSuccessListener(doc -> adapter.addCheck(doc));

            ratingBar.setRating(0);
            etNote.setText("");
        }).addOnFailureListener(e -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void checkAndUnlockBadge(String uid) {
        db.collection("users")
                .document(uid)
                .collection("prepost_checks")
                .whereGreaterThanOrEqualTo("rating", 4)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.size() >= 5) {
                        unlockBadge(uid);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "检查徽章失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void unlockBadge(String uid) {
        db.collection("users")
                .document(uid)
                .collection("badges")
                .document("badge_1")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) return; // 已经解锁过，不重复弹窗

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("unlocked", true);
                    data.put("timestamp", System.currentTimeMillis());
                    data.put("description", "有 5 次呼吸评分都达到 4 分以上，说明你越来越懂得照顾自己了！");

                    db.collection("users")
                            .document(uid)
                            .collection("badges")
                            .document("badge_1")
                            .set(data);

                    showBadgePopup();
                });
    }

    private void showBadgePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎉 恭喜获得徽章！");
        builder.setMessage("你已经连续获得 5 次高评分呼吸记录！");

        builder.setPositiveButton("查看徽章", (dialog, which) -> {
            Intent intent = new Intent(this, BadgeActivity.class);
            intent.putExtra("newBadge", "badge_2");
            startActivity(intent);
        });

        builder.setNegativeButton("关闭", null);
        builder.show();
    }
}
