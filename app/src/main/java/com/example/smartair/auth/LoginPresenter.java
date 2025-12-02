package com.example.smartair.auth;

import com.example.smartair.models.UserRole;

public class LoginPresenter implements LoginContract.Presenter, AuthModel.AuthCallback {

    private LoginContract.View view;
    private final AuthModel model;

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public LoginPresenter(LoginContract.View view, AuthModel model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void onLoginClicked() {
        String email = view.getEmail().trim();
        String password = view.getPassword();

        if (email.isEmpty()) {
            view.showError("Email cannot be empty");
            return;
        }

        if (password.isEmpty()) {
            view.showError("Password cannot be empty");
            return;
        }

        if (!email.matches(EMAIL_PATTERN)) {
            view.showError("Invalid email format");
            return;
        }

        if (password.length() < 6) {
            view.showError("Password must be at least 6 characters");
            return;
        }

        view.showLoading();
        model.login(email, password, this);
    }

    @Override
    public void onSuccess(UserRole role) {
        if (view != null) {
            view.hideLoading();
            view.navigateToHome(role);
        }
    }

    @Override
    public void onError(String error) {
        if (view != null) {
            view.hideLoading();
            view.showError(error);
        }
    }

    @Override
    public void onDestroy() {
        view = null;
    }
}