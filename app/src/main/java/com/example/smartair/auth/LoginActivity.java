package com.example.smartair.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.HomepageActivity;
import com.example.smartair.HomepageParentsActivity;
import com.example.smartair.HomepageProvidersActivity;
import com.example.smartair.R;
import com.example.smartair.models.UserRole;
import com.example.smartair.utils.SharedPrefsHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private static final String TAG = "LoginActivity";

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private ProgressBar progressBar;

    private LoginContract.Presenter presenter;
    private SharedPrefsHelper prefsHelper;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            Log.e(TAG, "Firebase init failed", e);
            Toast.makeText(this, "Firebase initialization failed", Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        prefsHelper = new SharedPrefsHelper(this);

        AuthModel model = new AuthModel();
        presenter = new LoginPresenter(this, model);

        loginButton.setOnClickListener(v -> {
            presenter.onLoginClicked();
        });

        registerTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        loginButton.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(ProgressBar.GONE);
        loginButton.setEnabled(true);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToHome(UserRole role) {
        FirebaseUser user = AuthModel.mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String roleStr = doc.getString("role");
                        if (roleStr == null) roleStr = "child";

                        UserRole userRole = UserRole.fromString(roleStr);
                        prefsHelper.saveUserRole(userRole.getValue());

                        switch (userRole) {
                            case CHILD:
                                startActivity(new Intent(this, HomepageActivity.class));
                                break;
                            case PARENT:
                                startActivity(new Intent(this, HomepageParentsActivity.class));
                                break;
                            case PROVIDER:
                                startActivity(new Intent(this, HomepageProvidersActivity.class));
                                break;
                        }
                        finish();
                    } else {
                        Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch user role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public String getEmail() {
        return emailEditText.getText().toString().trim();
    }

    @Override
    public String getPassword() {
        return passwordEditText.getText().toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}