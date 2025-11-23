package com.example.smartair.homepages;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartair.R;
import com.example.smartair.child_managent.AddChildActivity;
import com.example.smartair.child_managent.ChooseChildForSharingActivity;
import com.example.smartair.child_managent.ManageChildActivity;
import com.example.smartair.child_managent.ViewChildrenActivity;
import com.example.smartair.ui.parent.ParentHomeActivity;

public class HomepageParentsActivity extends AppCompatActivity {

    private Button btnAddChild;
    private Button btnViewChildren;
    private Button btnManageChild;
    private Button btnManageSharing;
    private Button btnManagePB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_parents); // 你的新布局文件

        // 初始化按钮
        btnAddChild = findViewById(R.id.btnAddChild);
        btnViewChildren = findViewById(R.id.btnViewChildren);
        btnManageChild = findViewById(R.id.btnManageChild);
        btnManageSharing = findViewById(R.id.btnManageSharing);
        btnManagePB = findViewById(R.id.btnManagePB);


        // 设置点击事件
        btnAddChild.setOnClickListener(v ->
                startActivity(new Intent(this, AddChildActivity.class)));

        btnViewChildren.setOnClickListener(v ->
                startActivity(new Intent(this, ViewChildrenActivity.class)));
        btnManageChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChooseChildForSharingActivity.class);
            intent.putExtra("mode", "manageChild");
            startActivity(intent);
        });
        btnManageSharing.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChooseChildForSharingActivity.class);
            intent.putExtra("mode", "sharing");
            startActivity(intent);
        });
        btnManagePB.setOnClickListener(v ->
                startActivity(new Intent(this, ParentHomeActivity.class)));
    }
}