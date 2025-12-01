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

import com.example.smartair.R;
import com.example.smartair.homepages.HomepageActivity;
import com.example.smartair.homepages.HomepageParentsActivity;
import com.example.smartair.homepages.HomepageProvidersActivity;
import com.example.smartair.models.UserRole;
import com.example.smartair.utils.SharedPrefsHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private static final String TAG = "LoginActivity";

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private ProgressBar progressBar;

    private TextView textForgotPassword;
    private LoginContract.Presenter presenter;
    private SharedPrefsHelper prefsHelper;
    private FirebaseFirestore db;
    private boolean isChildLogin = false;


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

        textForgotPassword = findViewById(R.id.textForgotPassword);

        textForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        db = FirebaseFirestore.getInstance();
        prefsHelper = new SharedPrefsHelper(this);

        AuthModel model = new AuthModel();
        presenter = new LoginPresenter(this, model);

        loginButton.setOnClickListener(v -> handleLogin());

        registerTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
    private void handleLogin() {
        String input = getEmail();
        String password = getPassword();

        if (input.isEmpty() || password.isEmpty()) {
            showError("Please enter login credentials");
            return;
        }
        if (!input.contains("@")) {
            loginChild(input, password);
            return;
        }

        presenter.onLoginClicked();
    }
    private void loginChild(String username, String password) {
        showLoading();

        FirebaseAuth.getInstance().signOut();

        db.collection("usernames")
                .document(username)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        hideLoading();
                        showError("Invalid username");
                        return;
                    }

                    String parentId = doc.getString("parentId");
                    String childUid = doc.getString("childUid");

                    if (parentId == null || childUid == null) {
                        hideLoading();
                        showError("Account is not configured correctly");
                        return;
                    }

                    db.collection("users")
                            .document(parentId)
                            .collection("children")
                            .document(childUid)
                            .get()
                            .addOnSuccessListener(childDoc -> {
                                hideLoading();

                                if (!childDoc.exists()) {
                                    hideLoading();
                                    showError("Child profile missing");
                                    return;
                                }

                                String savedPw = childDoc.getString("password");
                                String email = childDoc.getString("email");

                                if (!password.equals(savedPw)) {
                                    hideLoading();
                                    showError("Incorrect password");
                                    return;
                                }

                                if (email == null) {
                                    hideLoading();
                                    showError("Missing email for child account");
                                    return;
                                }

                                FirebaseAuth.getInstance()
                                        .signInWithEmailAndPassword(email, password)
                                        .addOnSuccessListener(authResult -> {
                                            hideLoading();

                                            prefsHelper.saveUserRole("child");
                                prefsHelper.saveUserRole("child");
                                prefsHelper.saveUserId(childUid);
                                prefsHelper.saveParentId(parentId);

                                            // IMPORTANT: Use the CHILD homepage
                                            startActivity(new Intent(this, HomepageActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            hideLoading();
                                            showError("Auth login failed: " + e.getMessage());
                                        });

                            })
                            .addOnFailureListener(e -> {
                                hideLoading();
                                showError("Failed to load child account");
                            });

                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    showError("Login failed: " + e.getMessage());
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

        // CHILD LOGIN FLOW
        if (isChildLogin) {
            prefsHelper.saveUserRole("child");

            Intent intent;
            if (!prefsHelper.isOnboardingComplete()) {
                intent = new Intent(this, com.example.smartair.onboarding.OnboardingActivity.class);
                intent.putExtra("userRole", "child");
            } else {
                intent = new Intent(this, HomepageActivity.class);
            }

            startActivity(intent);
            finish();
            return;
        }

        // NORMAL LOGIN FLOW (parent/provider)
        SharedPrefsHelper.saveString(LoginActivity.this, "PARENT_EMAIL", getEmail());
        SharedPrefsHelper.saveString(LoginActivity.this, "PARENT_PASSWORD", getPassword());


        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String roleStr = doc.getString("role");
                    if (roleStr == null) roleStr = "child";

                    UserRole userRole = UserRole.fromString(roleStr);
                    prefsHelper.saveUserRole(userRole.getValue());
                    prefsHelper.saveUserId(user.getUid());

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
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch user role: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
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