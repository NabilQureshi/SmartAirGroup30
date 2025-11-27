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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private RadioGroup roleRadioGroup;
    private Button registerButton;
    private ProgressBar progressBar;
    private TextView backToLoginTextView;

    private AuthModel authModel;
    private FirebaseFirestore db;
    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
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
        db = FirebaseFirestore.getInstance();

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
            Toast.makeText(this, "Invalid email address.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show();
            return;
        }

        // 固定写死 role，也可以按界面选择
        UserRole role = UserRole.CHILD;
        int checkedId = roleRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioParent) role = UserRole.PARENT;
        else if (checkedId == R.id.radioProvider) role = UserRole.PROVIDER;

        progressBar.setVisibility(android.view.View.VISIBLE);
        registerButton.setEnabled(false);

        // 超时处理
        timeoutRunnable = () -> {
            progressBar.setVisibility(android.view.View.GONE);
            registerButton.setEnabled(true);
            Toast.makeText(RegisterActivity.this, "Request timed out.", Toast.LENGTH_LONG).show();
        };
        timeoutHandler.postDelayed(timeoutRunnable, 15000);

        authModel.register(email, password, role, new AuthModel.AuthCallback() {
            @Override
            public void onSuccess(UserRole r) {
                timeoutHandler.removeCallbacks(timeoutRunnable);

                // 注册成功后把用户信息写入 Firestore
                String uid = authModel.mAuth.getCurrentUser().getUid();
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("name", name);
                userMap.put("email", email);
                userMap.put("role", r.getValue()); // ✅ 用枚举 getValue 写入 Firestore

                db.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(android.view.View.GONE);
                            registerButton.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_LONG).show();
                            finish(); // 返回登录页
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(android.view.View.GONE);
                            registerButton.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

            @Override
            public void onError(String message) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                progressBar.setVisibility(android.view.View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Registration failed: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}