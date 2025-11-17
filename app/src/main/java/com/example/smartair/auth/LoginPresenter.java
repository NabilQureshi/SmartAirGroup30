package com.example.smartair.auth;

import android.text.TextUtils;
import android.util.Patterns;

import com.example.smartair.models.UserRole;

public class LoginPresenter implements LoginContract.Presenter {

    private LoginContract.View view;
    private final AuthModel model;

    public LoginPresenter(LoginContract.View view, AuthModel model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void onLoginClicked() {
        if (view == null) {
            return;
        }

        String email = view.getEmail();
        String password = view.getPassword();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            view.showError("Email and password are required.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showError("Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            view.showError("Password must be at least 6 characters.");
            return;
        }

        view.showLoading();

        model.login(email, password, new AuthModel.AuthCallback() {
            @Override
            public void onSuccess(UserRole role) {
                if (view == null) return;
                view.hideLoading();
                view.navigateToHome(role);
            }

            @Override
            public void onError(String message) {
                if (view == null) return;
                view.hideLoading();
                view.showError("Login failed: " + message);
            }
        });
    }

    @Override
    public void onDestroy() {
        view = null;
    }
}
