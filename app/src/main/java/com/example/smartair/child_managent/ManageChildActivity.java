package com.example.smartair;

import androidx.appcompat.app.AlertDialog;

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
    private Button btnSave, btnDelete, btnInviteProvider;
    private Button btnManageSharing;
    private String childId, parentId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_child);

        db = FirebaseFirestore.getInstance();
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        childId = getIntent().getStringExtra("childId");
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        String name = getIntent().getStringExtra("name");
        String dob = getIntent().getStringExtra("dob");
        String notes = getIntent().getStringExtra("notes");

        editChildUsername = findViewById(R.id.editChildUsername);
        editChildPassword = findViewById(R.id.editChildPassword);
        editChildName = findViewById(R.id.editChildName);
        editChildDOB = findViewById(R.id.editChildDOB);
        editChildNotes = findViewById(R.id.editChildNotes);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        btnManageSharing = findViewById(R.id.btnManageSharing);
        btnInviteProvider = findViewById(R.id.btnInviteProvider);

        btnManageSharing.setOnClickListener(v -> {
            Intent intent = new Intent(ManageChildActivity.this, ManageSharingActivity.class);
            intent.putExtra("childId", childId);
            startActivity(intent);
        });
        btnInviteProvider.setOnClickListener(v -> {
            Intent intent = new Intent(ManageChildActivity.this, InviteProviderActivity.class);
            intent.putExtra("CHILD_ID", childId);
            startActivity(intent);
        });

        editChildUsername.setText(username);
        editChildPassword.setText(password);
        editChildName.setText(name);
        editChildDOB.setText(dob);
        editChildNotes.setText(notes);

        btnSave.setOnClickListener(v -> updateChild());

        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void updateChild() {
        String name = editChildName.getText().toString().trim();
        String username = editChildUsername.getText().toString().trim();
        String password = editChildPassword.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Child name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("password", password);
        updates.put("name", name);
        updates.put("dob", editChildDOB.getText().toString().trim());
        updates.put("notes", editChildNotes.getText().toString().trim());

        db.collection("users")
                .document(parentId)
                .collection("children")
                .document(childId)
                .update(updates)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Child updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Child")
                .setMessage("Are you sure you want to permanently delete this child's profile?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteChild();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void deleteChild() {
        db.collection("users")
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