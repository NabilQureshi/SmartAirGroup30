package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ParentChildSelectActivity extends AppCompatActivity {

    private LinearLayout layout;
    private FirebaseFirestore db;
    private String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_child_select);

        layout = findViewById(R.id.childListContainer);
        db = FirebaseFirestore.getInstance();

        // Make sure someone is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "No parent logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load children from: users/{parentId}/children
        db.collection("users")
                .document(parentId)
                .collection("children")
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(this, "No children linked to this parent.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot doc : snap) {
                        String childId = doc.getId();
                        String childName = doc.getString("name");
                        if (childName == null || childName.isEmpty()) {
                            childName = childId;
                        }

                        Button b = new Button(this);
                        b.setText("Check-in for: " + childName);

                        final String finalChildId = childId;
                        b.setOnClickListener(v -> openChildCheckin(finalChildId));

                        layout.addView(b);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading children: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void openChildCheckin(String childUid) {
        Intent i = new Intent(ParentChildSelectActivity.this, SymptomCheckInActivity.class);
        i.putExtra("childId", childUid);
        i.putExtra("openedByParent", true);
        startActivity(i);
    }
}