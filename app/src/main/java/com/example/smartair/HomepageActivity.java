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

    // 🔥 Add your testing buttons for R2
    private Button btnAddChild;
    private Button btnViewChildren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Existing UI
        btnLogMedicine = findViewById(R.id.btnLogMedicine);
        btnTechnique = findViewById(R.id.btnTechnique);
        btnPrePostCheck = findViewById(R.id.btnPrePostCheck);

        // 🔥 New R2 testing buttons
        btnAddChild = findViewById(R.id.btnAddChild);
        btnViewChildren = findViewById(R.id.btnViewChildren);


        // 🔥 ADD CHILD (your code)
        btnAddChild.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, AddChildActivity.class);
            startActivity(intent);
        });

        // 🔥 VIEW CHILDREN (your code)
        btnViewChildren.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, ViewChildrenActivity.class);
            startActivity(intent);
        });
    }
}
