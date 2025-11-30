package com.example.smartair.checkin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HistorySelectChildActivity extends AppCompatActivity {

    private LinearLayout childListContainer;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_child_select);

        childListContainer = findViewById(R.id.childListContainer);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Parent not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // children are stored under: users/{parentId}/children
        db.collection("users")
                .document(parentId)
                .collection("children")
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(this, "No children linked", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot doc : snap) {
                        String childUid = doc.getId();
                        String name = doc.getString("name");

                        Button btn = new Button(this);
                        btn.setText("View History: " +
                                (name != null && !name.isEmpty() ? name : childUid));

                        btn.setOnClickListener(v -> openHistoryForChild(childUid));
                        childListContainer.addView(btn);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading children: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void openHistoryForChild(String childUid) {
        Intent intent = new Intent(this, SymptomHistoryActivity.class);
        intent.putExtra("childId", childUid);
        startActivity(intent);
    }
}
