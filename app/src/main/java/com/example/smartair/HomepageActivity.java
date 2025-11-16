package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomepageActivity extends AppCompatActivity {

    private Button btnLogMedicine;
    private Button btnTechnique;
    private Button btnPrePostCheck;

    private Button btnBadges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage); // 确保布局文件存在

        // 获取按钮控件
        btnLogMedicine = findViewById(R.id.btnLogMedicine);
        btnTechnique = findViewById(R.id.btnTechnique);
        btnPrePostCheck = findViewById(R.id.btnPrePostCheck);
        btnBadges = findViewById(R.id.btnBadges);

        // 点击跳转到 LogMedicineActivity
        btnLogMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomepageActivity.this, LogMedicineActivity.class);
                startActivity(intent);
            }
        });

        // 点击跳转到 TechniqueActivity
        btnTechnique.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomepageActivity.this, TechniqueActivity.class);
                startActivity(intent);
            }
        });

        // 点击跳转到 PrePostCheckActivity
        btnPrePostCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomepageActivity.this, PrePostCheckActivity.class);
                startActivity(intent);
            }
        });

        // 点击跳转到BadgeActivity
        btnBadges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomepageActivity.this, BadgeActivity.class);
                startActivity(intent);
            }
        });
    }
}
