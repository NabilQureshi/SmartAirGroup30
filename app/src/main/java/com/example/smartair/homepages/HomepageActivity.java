package com.example.smartair.homepages;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.example.smartair.R;

import com.example.smartair.badges_system.BadgeActivity;
import com.example.smartair.medicine_logs.LogMedicineActivity;
import com.example.smartair.pre_post_checks.PrePostCheckActivity;
import com.example.smartair.technique_guidance.TechniqueActivity;
import com.example.smartair.ui.child.ChildPEFActivity;
import com.example.smartair.ui.child.ChildTriageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class HomepageActivity extends AppCompatActivity {

    private Button btnLogMedicine;
    private Button btnTechnique;
    private Button btnPrePostCheck;
    private Button btnBadge; // 只声明，不初始化
    private Button btnCheckPeakFlow;
    private Button btnCheckSymptom;
    private TextView textGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        // 确保布局文件存在
        textGreeting = findViewById(R.id.textGreeting);
        // 初始化按钮
        btnLogMedicine = findViewById(R.id.btnLogMedicine);
        btnTechnique = findViewById(R.id.btnTechnique);
        btnPrePostCheck = findViewById(R.id.btnPrePostCheck);
        btnBadge = findViewById(R.id.btnBadges); // ✅ 放到这里初始化
        btnCheckPeakFlow = findViewById(R.id.btnCheckPeakFlow);
        btnCheckSymptom = findViewById(R.id.btnCheckSymptom);


        btnLogMedicine.setOnClickListener(v ->
                startActivity(new Intent(this, LogMedicineActivity.class)));

        btnTechnique.setOnClickListener(v ->
                startActivity(new Intent(this, TechniqueActivity.class)));

        btnPrePostCheck.setOnClickListener(v ->
                startActivity(new Intent(this, PrePostCheckActivity.class)));

        btnBadge.setOnClickListener(v ->
                startActivity(new Intent(this, BadgeActivity.class)));
        btnCheckPeakFlow.setOnClickListener(v ->
                startActivity(new Intent(this, ChildPEFActivity.class)));
        btnCheckSymptom.setOnClickListener(v ->
                startActivity(new Intent(this, ChildTriageActivity.class)));
        loadChildUsername();
    }
    private void loadChildUsername() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");

                        if (username != null && !username.isEmpty()) {
                            textGreeting.setText("Hello, " + username + "!");
                        } else {
                            textGreeting.setText("Hello, Child!");
                        }
                    } else {
                        textGreeting.setText("Hello, Child!");
                    }
                })
                .addOnFailureListener(e -> {
                    textGreeting.setText("Hello, Child!");
                });
    }

}

