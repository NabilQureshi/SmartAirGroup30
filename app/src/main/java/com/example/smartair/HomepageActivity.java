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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        btnLogMedicine = findViewById(R.id.btnLogMedicine);
        btnTechnique = findViewById(R.id.btnTechnique);
        btnPrePostCheck = findViewById(R.id.btnPrePostCheck);
        btnAddChild = findViewById(R.id.btnAddChild);
        btnViewChildren = findViewById(R.id.btnViewChildren);
        btnAddChild.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, AddChildActivity.class);
            startActivity(intent);
        });
        btnViewChildren.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, ViewChildrenActivity.class);
            startActivity(intent);
        });
    }
}
