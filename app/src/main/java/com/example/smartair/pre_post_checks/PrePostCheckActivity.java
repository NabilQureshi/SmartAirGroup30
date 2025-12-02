package com.example.smartair.pre_post_checks;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.smartair.utils.SharedPrefsHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
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
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference checkRef;
    private FirebaseUser user;
    private String targetUid;
    private String targetEmail;

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

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String savedRole = prefs.getUserRole();
        String savedChildId = prefs.getUserId();

        if ("child".equalsIgnoreCase(savedRole) && savedChildId != null) {
            targetUid = savedChildId;
            targetEmail = null;
        } else if (user != null) {
            targetUid = user.getUid();
            targetEmail = user.getEmail();
        } else {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkRef = db.collection("users")
                .document(targetUid)
                .collection("prepost_checks");

        adapter = new PrePostCheckAdapter();
        rvCheckHistory.setAdapter(adapter);
        rvCheckHistory.setLayoutManager(new LinearLayoutManager(this));

        checkRef.orderBy("timestamp").limit(50).get()
                .addOnSuccessListener(queryDocumentSnapshots -> adapter.setChecks(queryDocumentSnapshots.getDocuments()))
                .addOnFailureListener(e -> Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());

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

        long now = System.currentTimeMillis();

        HashMap<String, Object> data = new HashMap<>();
        data.put("uid", targetUid);
        data.put("email", targetEmail);
        data.put("when", when);
        data.put("result", result);
        data.put("rating", rating);
        data.put("note", note);
        data.put("timestamp", now);

        btnSubmit.setEnabled(false);
        checkRef.add(data).addOnSuccessListener(documentReference -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(this, "记录已保存", Toast.LENGTH_SHORT).show();

            documentReference.get().addOnSuccessListener(doc -> adapter.addCheck(doc));

            ratingBar.setRating(0);
            etNote.setText("");

            checkAndUnlockBadge(targetUid);
        }).addOnFailureListener(e -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void unlockBadge(String uid) {
        db.collection("users")
                .document(uid)
                .collection("badges")
                .document("badge_1")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) return;

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("unlocked", true);
                    data.put("timestamp", System.currentTimeMillis());
                    data.put("achieved", true);
                    data.put("firstAchieved", Timestamp.now());
                    data.put("description", "You have achieved 5 breathing sessions with a rating of 4 or higher.");

                    db.collection("users")
                            .document(uid)
                            .collection("badges")
                            .document("badge_1")
                            .set(data);

                    showBadgePopup();
                });
    }

    private void checkAndUnlockBadge(String uid) {
        int THRESHOLD = 4;
        int REQUIRED = 5;

        db.collection("users")
                .document(uid)
                .collection("prepost_checks")
                .whereGreaterThanOrEqualTo("rating", THRESHOLD)
                .get()
                .addOnSuccessListener(snap -> {
                    int highCount = snap.size();
                    if (highCount >= REQUIRED) {
                        unlockBadge(uid);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "获取徽章状态失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showBadgePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("干得好！");
        builder.setMessage("你已完成 5 次评分 ≥4 的呼吸训练，解锁了一枚徽章！");

        builder.setPositiveButton("查看徽章", (dialog, which) -> {
            Intent intent = new Intent(this, BadgeActivity.class);
            intent.putExtra("newBadge", "badge_1");
            startActivity(intent);
        });

        builder.setNegativeButton("稍后查看", null);
        builder.show();
    }
}

