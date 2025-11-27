package com.example.smartair.unsure;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnRegister;
    private RadioGroup rgRole;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        rgRole = findViewById(R.id.rgRole);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        int selectedId = rgRole.getCheckedRadioButtonId();
        if(selectedId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rb = findViewById(selectedId);
        String role = rb.getText().toString().toLowerCase(); // child/parent/provider

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("role", role);

                        db.collection("users").document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                        finish(); // 返回登录页面
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
