package com.example.smartair.homepages;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.smartair.BaseActivity;
import com.example.smartair.R;
import com.example.smartair.providers.ShareLogActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomepageProvidersActivity extends BaseActivity {

    private TextView textGreeting;
    private Button btnSignOut;
    private Button btnShareLog;   // 新增按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_providers);

        textGreeting = findViewById(R.id.textGreeting);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnShareLog = findViewById(R.id.btnShareLog); // 绑定按钮

        btnSignOut.setOnClickListener(v -> signOut());

        // 👉 新增跳转逻辑
        btnShareLog.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageProvidersActivity.this, ShareLogActivity.class);
            startActivity(intent);
        });

        loadProviderName();
    }

    private void loadProviderName() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            textGreeting.setText("Welcome, " + name);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });
    }
}
