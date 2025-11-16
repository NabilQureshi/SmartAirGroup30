package com.example.smartair.auth;

import com.example.smartair.models.UserRole;

public interface LoginContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showError(String message);
        void navigateToHome(UserRole role);
        String getEmail();
        String getPassword();
    }

    interface Presenter {
        void onLoginClicked();
        void onDestroy();
    }

    interface Model {
        void login(String email, String password, AuthCallback callback);
    }

    interface AuthCallback {
        void onSuccess(String uid, UserRole role);
        void onFailure(String error);
    }
}