package com.example.smartair.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.models.User;
import com.example.smartair.models.UserRole;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private RadioGroup roleRadioGroup;
    private Button registerButton;
    private TextView backToLoginTextView;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        registerButton = findViewById(R.id.registerButton);
        backToLoginTextView = findViewById(R.id.backToLoginTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> handleRegistration());
        backToLoginTextView.setOnClickListener(v -> finish());
    }

    private void handleRegistration() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (!validateInput(name, email, password, confirmPassword)) {
            return;
        }

        UserRole selectedRole = getSelectedRole();
        if (selectedRole == null) {
            showError("Please select a role");
            return;
        }

        showLoading();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserToFirestore(uid, email, name, selectedRole);
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    showError("Registration failed: " + e.getMessage());
                });
    }

    private void saveUserToFirestore(String uid, String email, String name, UserRole role) {
        User user = new User(uid, email, role, name);

        firestore.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    hideLoading();
                    showSuccess("Registration successful!");
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    showError("Failed to save user data: " + e.getMessage());
                });
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format");
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private UserRole getSelectedRole() {
        int selectedId = roleRadioGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.radioParent) {
            return UserRole.PARENT;
        } else if (selectedId == R.id.radioProvider) {
            return UserRole.PROVIDER;
        } else if (selectedId == R.id.radioChild) {
            return UserRole.CHILD;
        }

        return null;
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        registerButton.setEnabled(true);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}