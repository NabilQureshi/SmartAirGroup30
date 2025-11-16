package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ManageChildActivity extends AppCompatActivity {

    private EditText editChildUsername, editChildPassword;
    private EditText editChildName, editChildDOB, editChildNotes;
    private Button btnSave, btnDelete;

    private String childId, parentId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_child);

        db = FirebaseFirestore.getInstance();
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get data from intent
        childId = getIntent().getStringExtra("childId");
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        String name = getIntent().getStringExtra("name");
        String dob = getIntent().getStringExtra("dob");
        String notes = getIntent().getStringExtra("notes");

        // Connect views
        editChildUsername = findViewById(R.id.editChildUsername);
        editChildPassword = findViewById(R.id.editChildPassword);
        editChildName = findViewById(R.id.editChildName);
        editChildDOB = findViewById(R.id.editChildDOB);
        editChildNotes = findViewById(R.id.editChildNotes);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        Button btnManageSharing = findViewById(R.id.btnManageSharing);

        btnManageSharing.setOnClickListener(v -> {
            Intent intent = new Intent(ManageChildActivity.this, ManageSharingActivity.class);
            intent.putExtra("childId", childId);
            startActivity(intent);
        });

        // Pre-fill fields
        editChildUsername.setText(username);
        editChildPassword.setText(password);
        editChildName.setText(name);
        editChildDOB.setText(dob);
        editChildNotes.setText(notes);

        // Button actions
        btnSave.setOnClickListener(v -> updateChild());
        btnDelete.setOnClickListener(v -> deleteChild());
    }

    private void updateChild() {
        Map<String, Object> updates = new HashMap<>();

        updates.put("username", editChildUsername.getText().toString().trim());
        updates.put("password", editChildPassword.getText().toString().trim());
        updates.put("name", editChildName.getText().toString().trim());
        updates.put("dob", editChildDOB.getText().toString().trim());
        updates.put("notes", editChildNotes.getText().toString().trim());

        db.collection("parents")
                .document(parentId)
                .collection("children")
                .document(childId)
                .update(updates)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Child updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void deleteChild() {
        db.collection("parents")
                .document(parentId)
                .collection("children")
                .document(childId)
                .delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Child deleted.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
