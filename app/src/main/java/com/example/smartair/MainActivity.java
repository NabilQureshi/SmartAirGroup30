package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoSignUp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoSignUp = findViewById(R.id.btnGoSignUp);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnGoSignUp.setOnClickListener(v -> startActivity(new Intent(this, com.example.smartair.unsure.SignUpActivity.class)));

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null) {
                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        if(doc.exists()) {
                                            String role = doc.getString("role");
                                            if(role == null) role = "child";
                                            switch(role) {
                                                case "child":
                                                    startActivity(new Intent(this, com.example.smartair.homepages.HomepageActivity.class));
                                                    break;
                                                case "parent":
                                                    startActivity(new Intent(this, com.example.smartair.homepages.HomepageParentsActivity.class));
                                                    break;
                                                case "provider":
                                                    startActivity(new Intent(this, com.example.smartair.homepages.HomepageProvidersActivity.class));
                                                    break;
                                            }
                                            finish();
                                        } else {
                                            Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}