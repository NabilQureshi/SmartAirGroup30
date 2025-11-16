package com.example.smartair.auth;

import android.text.TextUtils;
import android.util.Patterns;

public class LoginPresenter implements LoginContract.Presenter {
    private final LoginContract.View view;
    private final LoginContract.Model model;

    public LoginPresenter(LoginContract.View view, LoginContract.Model model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void onLoginClicked() {
        String email = view.getEmail();
        String password = view.getPassword();

        if (!validateInput(email, password)) {
            return;
        }

        view.showLoading();

        model.login(email, password, new LoginContract.AuthCallback() {
            @Override
            public void onSuccess(String uid, com.example.smartair.models.UserRole role) {
                view.hideLoading();
                view.navigateToHome(role);
            }

            @Override
            public void onFailure(String error) {
                view.hideLoading();
                view.showError(error);
            }
        });
    }

    @Override
    public void onDestroy() {
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            view.showError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showError("Invalid email format");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            view.showError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            view.showError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }
}