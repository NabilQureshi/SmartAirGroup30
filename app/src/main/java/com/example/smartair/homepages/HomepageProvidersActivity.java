package com.example.smartair.homepages;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartair.BaseActivity;
import com.example.smartair.R;
import com.example.smartair.proviers.ProviderLinkedChildrenActivity;
import com.example.smartair.proviers.ShareLogActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomepageProvidersActivity extends BaseActivity {

    private TextView textGreeting;
    private Button btnSignOut;
    private Button btnShareLog;
    private Button btnLinkedChildren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_providers);

        textGreeting = findViewById(R.id.textGreeting);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnShareLog = findViewById(R.id.btnShareLog);
        btnLinkedChildren = findViewById(R.id.btnLinkedChildren);

        btnSignOut.setOnClickListener(v -> signOut());

        btnShareLog.setOnClickListener(v ->
                startActivity(new Intent(this, ShareLogActivity.class))
        );

        btnLinkedChildren.setOnClickListener(v ->
                startActivity(new Intent(this, ProviderLinkedChildrenActivity.class))
        );

        loadProviderName();
    }

    private void loadProviderName() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

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
                });
    }
}

