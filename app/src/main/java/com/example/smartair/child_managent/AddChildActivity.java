package com.example.smartair.child_managent;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddChildActivity extends AppCompatActivity {

    private EditText editChildUsername, editChildPassword;
    private EditText editChildName, editChildNotes;

    private TextView textDOB;
    private Button btnPickDate, btnAddChild;
    private String selectedDOB = "";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser parent = auth.getCurrentUser();
        if (parent == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editChildUsername = findViewById(R.id.editChildUsername);
        editChildPassword = findViewById(R.id.editChildPassword);
        editChildName = findViewById(R.id.editChildName);
        editChildNotes = findViewById(R.id.editChildNotes);
        btnAddChild = findViewById(R.id.btnAddChild);

        textDOB = findViewById(R.id.textDOB);
        btnPickDate = findViewById(R.id.btnPickDate);

        btnPickDate.setOnClickListener(v -> showDatePickerDialog());
        btnAddChild.setOnClickListener(v -> checkUsernameAndAddChild());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, yr, mo, dy) -> {
                    String dob = String.format("%02d/%02d/%d", dy, mo + 1, yr);
                    selectedDOB = dob;
                    textDOB.setText("Date of Birth: " + dob);
                },
                year, month, day
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void checkUsernameAndAddChild() {
        String username = editChildUsername.getText().toString().trim();
        String password = editChildPassword.getText().toString().trim();
        String name = editChildName.getText().toString().trim();
        String dob = selectedDOB;
        String notes = editChildNotes.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Name and DOB required.", Toast.LENGTH_SHORT).show();
            return;
        }

        String generatedEmail = username + "@child.smartair.com";

        FirebaseUser parent = auth.getCurrentUser();
        if (parent == null) {
            Toast.makeText(this, "Session expired. Log in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String parentId = parent.getUid();

        DocumentReference usernameRef = db.collection("usernames").document(username);

        usernameRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Toast.makeText(this, "Username already exists.", Toast.LENGTH_LONG).show();
            } else {
                saveChildFirestore(
                        parent.getUid(),
                        generatedEmail,
                        password,
                        username,
                        name,
                        dob,
                        notes
                );
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Username check failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveChildFirestore(
            String parentId,
            String email,
            String password,
            String username,
            String name,
            String dob,
            String notes
    ) {
        WriteBatch batch = db.batch();

        // Create a new child document with random ID
        DocumentReference childRef = db.collection("users")
                .document(parentId)
                .collection("children")
                .document();

        String childUid = childRef.getId();

        DocumentReference usernameRef = db.collection("usernames").document(username);

        Map<String, Object> child = new HashMap<>();
        child.put("uid", childUid);
        child.put("email", email);
        child.put("password", password);
        child.put("username", username);
        child.put("name", name);
        child.put("dob", dob);
        child.put("notes", notes);
        child.put("createdAt", FieldValue.serverTimestamp());

        Map<String, Object> usernameMap = new HashMap<>();
        usernameMap.put("email", email);
        usernameMap.put("childUid", childUid);
        usernameMap.put("parentId", parentId);

        batch.set(childRef, child);
        batch.set(usernameRef, usernameMap);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Child added successfully!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this, ViewChildrenActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firestore save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
