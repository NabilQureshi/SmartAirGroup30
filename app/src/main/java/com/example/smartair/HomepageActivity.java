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
    private Button btnBadge; // 只声明，不初始化

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage); // 确保布局文件存在

        // 初始化按钮
        btnLogMedicine = findViewById(R.id.btnLogMedicine);
        btnTechnique = findViewById(R.id.btnTechnique);
        btnPrePostCheck = findViewById(R.id.btnPrePostCheck);
        btnAddChild = findViewById(R.id.btnAddChild);
        btnViewChildren = findViewById(R.id.btnViewChildren);
        btnManageChild = findViewById(R.id.btnManageChild);
        btnManageSharing = findViewById(R.id.btnManageSharing);
        btnBadge = findViewById(R.id.btnBadges); // ✅ 放到这里初始化

        // 设置点击事件
        btnAddChild.setOnClickListener(v ->
                startActivity(new Intent(this, AddChildActivity.class)));

        btnViewChildren.setOnClickListener(v ->
                startActivity(new Intent(this, ViewChildrenActivity.class)));

        btnManageChild.setOnClickListener(v ->
                startActivity(new Intent(this, ManageChildActivity.class)));

        btnManageSharing.setOnClickListener(v ->
                startActivity(new Intent(this, ChooseChildForSharingActivity.class)));

        btnLogMedicine.setOnClickListener(v ->
                startActivity(new Intent(this, LogMedicineActivity.class)));

        btnTechnique.setOnClickListener(v ->
                startActivity(new Intent(this, TechniqueActivity.class)));

        btnPrePostCheck.setOnClickListener(v ->
                startActivity(new Intent(this, PrePostCheckActivity.class)));

        btnBadge.setOnClickListener(v ->
                startActivity(new Intent(this, BadgeActivity.class)));
    }
}
