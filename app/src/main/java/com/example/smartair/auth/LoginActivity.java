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
import com.example.smartair.R;
import com.example.smartair.models.UserRole;
import com.example.smartair.utils.SharedPrefsHelper;
import com.google.firebase.FirebaseApp;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private static final String TAG = "LoginActivity";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private ProgressBar progressBar;

    private LoginContract.Presenter presenter;
    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            FirebaseApp.initializeApp(this); // 确保 Firebase 初始化
        } catch (Exception e) {
            Log.e(TAG, "Firebase 初始化失败", e);
            Toast.makeText(this, "Firebase 初始化失败", Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        progressBar = findViewById(R.id.progressBar);

        try {
            AuthModel model = new AuthModel();
            presenter = new LoginPresenter(this, model);
        } catch (Exception e) {
            Log.e(TAG, "Presenter 初始化失败", e);
            Toast.makeText(this, "登录模块初始化失败", Toast.LENGTH_LONG).show();
        }

        prefsHelper = new SharedPrefsHelper(this);

        loginButton.setOnClickListener(v -> {
            try {
                if (presenter != null) {
                    presenter.onLoginClicked();
                } else {
                    Log.e(TAG, "Presenter 未初始化");
                    Toast.makeText(this, "登录模块未准备好", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "登录点击异常", e);
                Toast.makeText(this, "登录异常: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        registerTextView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "跳转注册异常", e);
                Toast.makeText(this, "无法跳转注册页面", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToHome(UserRole role) {
        prefsHelper.saveUserRole(role.getValue());
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
        try {
            if (presenter != null) {
                presenter.onDestroy();
            }
        } catch (Exception e) {
            Log.e(TAG, "Presenter 销毁异常", e);
        }
    }
}
