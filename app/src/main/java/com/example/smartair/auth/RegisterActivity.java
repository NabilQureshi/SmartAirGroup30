package com.example.smartair.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.models.UserRole;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private RadioGroup roleRadioGroup;
    private Button registerButton;
    private ProgressBar progressBar;
    private TextView backToLoginTextView;

    private AuthModel authModel;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
        backToLoginTextView = findViewById(R.id.backToLoginTextView);

        authModel = new AuthModel();

        registerButton.setOnClickListener(v -> register());
        backToLoginTextView.setOnClickListener(v -> finish());
    }

    private void register() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show();
            return;
        }

        int checkedId = roleRadioGroup.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Please select a role.", Toast.LENGTH_LONG).show();
            return;
        }

        UserRole role;
        if (checkedId == R.id.radioParent) {
            role = UserRole.PARENT;
        } else if (checkedId == R.id.radioProvider) {
            role = UserRole.PROVIDER;
        } else {
            role = UserRole.CHILD;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        timeoutRunnable = () -> {
            progressBar.setVisibility(View.GONE);
            registerButton.setEnabled(true);
            Toast.makeText(RegisterActivity.this, "Request timed out. Check your connection or Firebase config.", Toast.LENGTH_LONG).show();
        };
        timeoutHandler.postDelayed(timeoutRunnable, 15000);

        authModel.register(email, password, role, new AuthModel.AuthCallback() {
            @Override
            public void onSuccess(UserRole r) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Sign up successful.", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Sign up failed: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}
