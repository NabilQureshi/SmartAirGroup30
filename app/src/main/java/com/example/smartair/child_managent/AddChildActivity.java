package com.example.smartair.child_managent;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.UUID;

public class AddChildActivity extends AppCompatActivity {

    private EditText editChildUsername, editChildPassword, editChildName, editChildNotes, editChildDOB;
    private TextView textGreeting;
    private String selectedDOB = "";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser parent = auth.getCurrentUser();
        if (parent == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textGreeting = findViewById(R.id.textGreeting);
        editChildUsername = findViewById(R.id.editChildUsername);
        editChildPassword = findViewById(R.id.editChildPassword);
        editChildName = findViewById(R.id.editChildName);
        editChildNotes = findViewById(R.id.editChildNotes);
        editChildDOB = findViewById(R.id.editChildDOB);

        editChildDOB.setOnClickListener(v -> showDatePickerDialog());
        findViewById(R.id.btnAddChild).setOnClickListener(v -> checkUsernameAndAddChild());

        loadParentName(parent.getUid());
    }

    private void loadParentName(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            textGreeting.setText("Hello, " + name);
                        }
                    }
                });
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
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

    private void checkUsernameAndAddChild() {
        String username = editChildUsername.getText().toString().trim();
        String password = editChildPassword.getText().toString().trim();
        String name = editChildName.getText().toString().trim();
        String dob = selectedDOB;
        String notes = editChildNotes.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "All fields required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check username uniqueness
        DocumentReference usernameRef = db.collection("usernames").document(username);
        usernameRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Toast.makeText(this, "Username already exists.", Toast.LENGTH_LONG).show();
            } else {
                // Create child WITHOUT FirebaseAuth account
                createChildRecord(username, password, name, dob, notes);
            }
        });
    }

    /**
     * Create a child entry WITHOUT FirebaseAuth account.
     * Child is identified by UUID and logs in through Firestore username lookup.
     */
    private void createChildRecord(String username, String password, String name, String dob, String notes) {
        String parentId = auth.getCurrentUser().getUid();
        String childUid = UUID.randomUUID().toString();

        saveChildFirestore(parentId, childUid, username, username + "@child.smartair.com", name, dob, notes, password);
    }

    /**
     * Store child in:
     *  - users/{parentId}/children/{childUid}
     *  - users/{childUid}
     *  - usernames/{username}
     */
    private void saveChildFirestore(String parentId, String childUid, String username, String email,
                                    String name, String dob, String notes, String password) {

        WriteBatch batch = db.batch();

        // users/{parentId}/children/{childUid}
        DocumentReference childRef = db.collection("users")
                .document(parentId)
                .collection("children")
                .document(childUid);

        Map<String, Object> childData = new HashMap<>();
        childData.put("uid", childUid);
        childData.put("username", username);
        childData.put("email", email);
        childData.put("password", password);
        childData.put("name", name);
        childData.put("dob", dob);
        childData.put("notes", notes);
        childData.put("role", "child");
        childData.put("createdAt", FieldValue.serverTimestamp());

        // users/{childUid}
        DocumentReference childUserRef = db.collection("users").document(childUid);
        Map<String, Object> childUserData = new HashMap<>(childData);
        childUserData.put("parentId", parentId);

        // usernames/{username}
        DocumentReference usernameRef = db.collection("usernames").document(username);
        Map<String, Object> usernameMap = new HashMap<>();
        usernameMap.put("childUid", childUid);
        usernameMap.put("parentId", parentId);
        usernameMap.put("email", email);
        usernameMap.put("password", password);

        batch.set(childRef, childData);
        batch.set(childUserRef, childUserData);
        batch.set(usernameRef, usernameMap);

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Child account created successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AddChildActivity.this, ChooseChildForSharingActivity.class);
                    intent.putExtra("parentId", parentId);
                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save Firestore data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}