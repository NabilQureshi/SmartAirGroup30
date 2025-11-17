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
    private Button btnAddChild;
    private Button btnViewChildren;
    private Button btnManageChild;
    private Button btnManageSharing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage); // 确保布局文件存在

        // 获取按钮控件
        btnLogMedicine = findViewById(R.id.btnLogMedicine);
        btnTechnique = findViewById(R.id.btnTechnique);
        btnPrePostCheck = findViewById(R.id.btnPrePostCheck);
        btnAddChild = findViewById(R.id.btnAddChild);
        btnViewChildren = findViewById(R.id.btnViewChildren);
        btnManageChild = findViewById(R.id.btnManageChild);
        btnManageSharing = findViewById(R.id.btnManageSharing);

        btnAddChild.setOnClickListener(v ->
                startActivity(new Intent(this, AddChildActivity.class)));

        btnViewChildren.setOnClickListener(v ->
                startActivity(new Intent(this, ViewChildrenActivity.class)));

        btnManageChild.setOnClickListener(v ->
                startActivity(new Intent(this, ManageChildActivity.class)));

        btnManageSharing.setOnClickListener(v -> {
            startActivity(new Intent(this, ChooseChildForSharingActivity.class));
        });


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
    }
}
