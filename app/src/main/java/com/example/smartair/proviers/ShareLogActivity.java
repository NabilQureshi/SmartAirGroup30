package com.example.smartair.providers;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.dashboard.DashboardProvidersActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class ShareLogActivity extends AppCompatActivity {

    private TextInputEditText etInviteCode;
    private MaterialButton btnSubmitInvite;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_log);

        db = FirebaseFirestore.getInstance();

        etInviteCode = findViewById(R.id.etInviteCode);
        btnSubmitInvite = findViewById(R.id.btnSubmitInvite);

        btnSubmitInvite.setOnClickListener(v -> handleInviteSubmit());
    }

    private void handleInviteSubmit() {
        String code = etInviteCode.getText() != null ? etInviteCode.getText().toString().trim() : "";

        if (TextUtils.isEmpty(code) || code.length() != 6) {
            Toast.makeText(this, "Invite code must be 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        validateInviteCode(code);
    }

    /**
     * Step 1: 根据 inviteCode 找到 Firestore 中对应的 childId + expires
     */
    private void validateInviteCode(String inviteCode) {
        db.collection("inviteCodes")
                .document(inviteCode)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Invalid invite code", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // inviteCodes/{inviteCode} 读取 childId
                    String uid = doc.getString("childId");
                    if (uid == null) {
                        Toast.makeText(this, "Invite code is corrupted", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 读取 expires 字段（java.util.Date）
                    Date expires = doc.getDate("expires");
                    if (expires == null) {
                        Toast.makeText(this, "Invite code missing expiration time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Date now = new Date();

                    // 判断是否过期
                    if (now.after(expires)) {
                        Toast.makeText(this, "Invite code has expired", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Step 2：没过期 → 加载用户数据
                    loadUserData(uid);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to validate invite code", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Step 2: 从 users/{uid} 读取 childId, username, password, name, dob, notes
     */
    private void loadUserData(String uid) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // --------- 从 Firestore 获取数据 ---------
                    String childId = doc.getString("uid");
                    String username = doc.getString("username");
                    String password = doc.getString("password");
                    String name = doc.getString("name");
                    String dob = doc.getString("dob");
                    String notes = doc.getString("notes");

                    if (childId == null) {
                        Toast.makeText(this, "User data incomplete: missing childId", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    goToDashboard(childId, username, password, name, dob, notes);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Step 3: 跳转 DashboardProvidersActivity
     */
    private void goToDashboard(String childId, String username, String password,
                               String name, String dob, String notes) {

        Intent intent = new Intent(ShareLogActivity.this, DashboardProvidersActivity.class);

        intent.putExtra("childId", childId);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("name", name);
        intent.putExtra("dob", dob);
        intent.putExtra("notes", notes);

        startActivity(intent);
        finish();
    }
}
