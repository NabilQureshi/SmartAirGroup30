package com.example.smartair.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button btnSendReset;
    private Button btnBackToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnSendReset.setOnClickListener(v -> sendPasswordResetEmail());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void sendPasswordResetEmail() {
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendReset.setEnabled(false);
        btnSendReset.setText("Sending...");

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnSendReset.setEnabled(true);
                    btnSendReset.setText("Send Reset Link");

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Password reset email sent! Check your inbox.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Failed to send reset email";
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}