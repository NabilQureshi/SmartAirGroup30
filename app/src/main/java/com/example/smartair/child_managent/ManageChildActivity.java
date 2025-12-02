package com.example.smartair.child_managent;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.sharing.InviteProviderActivity;
import com.example.smartair.sharing.ManageSharingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ManageChildActivity extends AppCompatActivity {

    private EditText editChildUsername, editChildPassword;
    private EditText editChildName, editChildNotes;
    private EditText editChildDOB;
    private Button btnSave, btnDelete, btnInviteProvider, btnManageSharing;

    private String childId;
    private String parentId;

    private FirebaseFirestore db;

    private String selectedDOB = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_child);

        db = FirebaseFirestore.getInstance();
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        childId = getIntent().getStringExtra("childId");

        if (childId == null) {
            Toast.makeText(this, "Error: No child selected.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize UI FIRST
        editChildUsername = findViewById(R.id.editChildUsername);
        editChildPassword = findViewById(R.id.editChildPassword);
        editChildName     = findViewById(R.id.editChildName);
        editChildDOB      = findViewById(R.id.editChildDOB);
        editChildNotes    = findViewById(R.id.editChildNotes);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnInviteProvider = findViewById(R.id.btnInviteProvider);
        btnManageSharing = findViewById(R.id.btnManageSharing);

        editChildDOB.setFocusable(false);
        editChildDOB.setOnClickListener(v -> showDobPicker());

        loadChildInfo();

        btnSave.setOnClickListener(v -> updateChild());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnManageSharing.setOnClickListener(v -> {
            Intent i = new Intent(ManageChildActivity.this, ManageSharingActivity.class);
            i.putExtra("childId", childId);
            startActivity(i);
        });

        btnInviteProvider.setOnClickListener(v -> {
            Intent i = new Intent(ManageChildActivity.this, InviteProviderActivity.class);
            i.putExtra("CHILD_ID", childId);
            startActivity(i);
        });
    }


    private void loadChildInfo() {
        db.collection("users")
                .document(parentId)
                .collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Child data not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    editChildUsername.setText(doc.getString("username"));
                    editChildPassword.setText(doc.getString("password"));
                    editChildName.setText(doc.getString("name"));
                    editChildDOB.setText(doc.getString("dob"));
                    editChildNotes.setText(doc.getString("notes"));
                    selectedDOB = doc.getString("dob");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load child data.", Toast.LENGTH_SHORT).show()
                );
    }

    private void showDobPicker() {
        final Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, yr, mo, dy) -> {
                    selectedDOB = String.format("%02d/%02d/%d", dy, mo + 1, yr);
                    editChildDOB.setText(selectedDOB);
                },
                year, month, day
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void updateChild() {
        String username = editChildUsername.getText().toString().trim();
        String password = editChildPassword.getText().toString().trim();
        String name = editChildName.getText().toString().trim();
        String notes = editChildNotes.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || selectedDOB.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("password", password);
        updates.put("name", name);
        updates.put("dob", selectedDOB);
        updates.put("notes", notes);

        db.collection("users")
                .document(parentId)
                .collection("children")
                .document(childId)
                .update(updates)
                .addOnSuccessListener(a -> Toast.makeText(this, "Child updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show());
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Child")
                .setMessage("Are you sure you want to permanently delete this child?")
                .setPositiveButton("Delete", (d, w) -> deleteChild())
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
                        Toast.makeText(this, "Delete failed.", Toast.LENGTH_SHORT).show()
                );
    }
}
