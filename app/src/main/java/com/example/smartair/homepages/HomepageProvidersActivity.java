package com.example.smartair.homepages;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.smartair.BaseActivity;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomepageProvidersActivity extends BaseActivity {

    private TextView textGreeting;
    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_providers);

        textGreeting = findViewById(R.id.textGreeting);
        btnSignOut = findViewById(R.id.btnSignOut);

        btnSignOut.setOnClickListener(v -> signOut());

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