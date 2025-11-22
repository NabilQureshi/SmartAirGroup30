package com.example.smartair;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private Button btnAddChild;

    // Date Picker UI
    private TextView textDOB;
    private Button btnPickDate;
    private String selectedDOB = "";

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

        DatePickerDialog.OnDateSetListener dateSetListener = (view, yearPicked, monthPicked, dayPicked) -> {
            int monthActual = monthPicked + 1;
            selectedDOB = String.format("%02d/%02d/%d", dayPicked, monthActual, yearPicked);
            textDOB.setText("Date of Birth: " + selectedDOB);
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void checkUsernameAndAddChild() {
        String username = editChildUsername.getText().toString().trim();
        String password = editChildPassword.getText().toString().trim();
        String name = editChildName.getText().toString().trim();
        String dob = selectedDOB;
        String notes = editChildNotes.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please enter name and select DOB.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference usernameRef = db.collection("usernames").document(username);

        usernameRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Toast.makeText(this, "Username '" + username + "' is already taken.", Toast.LENGTH_LONG).show();
            } else {
                saveChildWithBatch(user.getUid(), username, password, name, dob, notes);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void saveChildWithBatch(String parentId, String username, String password, String name, String dob, String notes) {

        Map<String, Object> childData = new HashMap<>();
        childData.put("username", username);
        childData.put("password", password);
        childData.put("name", name);
        childData.put("dob", dob);
        childData.put("notes", notes);
        childData.put("createdAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        DocumentReference newChildRef = db.collection("parents")
                .document(parentId)
                .collection("children")
                .document();

        DocumentReference usernameRef = db.collection("usernames").document(username);
        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("isTaken", true);
        usernameData.put("parentId", parentId);
        usernameData.put("childId", newChildRef.getId());

        batch.set(newChildRef, childData);
        batch.set(usernameRef, usernameData);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Child added successfully!", Toast.LENGTH_SHORT).show();

                    editChildUsername.setText("");
                    editChildPassword.setText("");
                    editChildName.setText("");
                    editChildNotes.setText("");
                    textDOB.setText("Date of Birth: Not Set");
                    selectedDOB = "";

                    Intent intent = new Intent(AddChildActivity.this, ViewChildrenActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving child: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}