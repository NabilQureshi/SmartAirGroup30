package com.example.smartair;

import java.util.Calendar;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.widget.TextView;

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

    private EditText editChildUsername, editChildPassword;
    private TextView textDOB;
    private Button btnPickDate;

    private String selectedDOB = "";

    private EditText editChildName, editChildNotes;
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


        editChildUsername = findViewById(R.id.editChildUsername);
        editChildPassword = findViewById(R.id.editChildPassword);

        editChildName = findViewById(R.id.editChildName);
        editChildNotes = findViewById(R.id.editChildNotes);
        btnAddChild = findViewById(R.id.btnAddChild);

        textDOB = findViewById(R.id.textDOB);
        btnPickDate = findViewById(R.id.btnPickDate);

        btnPickDate.setOnClickListener(v -> showDatePickerDialog());

        btnAddChild.setOnClickListener(v -> addChild());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener dateSetListener = (view, yearPicked, monthPicked, dayPicked) -> {

            // month picked starts from 0 thats why we add
            int monthActual = monthPicked + 1;

            selectedDOB = String.format("%02d/%02d/%d", dayPicked, monthActual, yearPicked);

            textDOB.setText("Date of Birth: " + selectedDOB);
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());//limits to the present

        datePickerDialog.show();
    }

    private void addChild() {
        String username = editChildUsername.getText().toString().trim();
        String password = editChildPassword.getText().toString().trim();
        String name = editChildName.getText().toString().trim();
        String dob = selectedDOB;
        String notes = editChildNotes.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter a username and password.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (name.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please enter a name and select a date of birth.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be signed in to add a child.", Toast.LENGTH_SHORT).show();
            return;
        }

        String parentId = user.getUid();

        Map<String, Object> child = new HashMap<>();
        child.put("username", username);
        child.put("childPin", password);
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
                        Toast.makeText(this, "Error adding child: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}