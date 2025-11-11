package com.example.smartair;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class AddChildActivity extends AppCompatActivity {
    private EditText editChildName, editChildDOB, editChildNotes;
    private Button btnAddChild;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            finish();
           return;
        }
        editChildName = findViewById(R.id.editChildName);
        editChildDOB = findViewById(R.id.editChildDOB);
        editChildNotes = findViewById(R.id.editChildNotes);
        btnAddChild = findViewById(R.id.btnAddChild);

        btnAddChild.setOnClickListener(v -> addChild());
    }

    private void addChild() {
        String name = editChildName.getText().toString().trim();
        String dob = editChildDOB.getText().toString().trim();
        String notes = editChildNotes.getText().toString().trim();

        if (name.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please enter name or/and date of birth.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be signed in to add a child.", Toast.LENGTH_SHORT).show();
            return;
        }
        String parentId = user.getUid();


        Map<String, Object> child = new HashMap<>();
        child.put("name", name);
        child.put("dob", dob);
        child.put("notes", notes);
        child.put("createdAt", FieldValue.serverTimestamp());

        db.collection("parents")
                .document(parentId)
                .collection("children")
                .add(child)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Child added successfully!", Toast.LENGTH_SHORT).show();
                    editChildName.setText("");
                    editChildDOB.setText("");
                    editChildNotes.setText("");
                    Intent intent = new Intent(AddChildActivity.this, ViewChildrenActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error adding child: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
