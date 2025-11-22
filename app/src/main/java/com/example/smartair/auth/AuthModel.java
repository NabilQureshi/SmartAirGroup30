package com.example.smartair.auth;

import androidx.annotation.NonNull;

import com.example.smartair.models.UserRole;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthModel {

    public interface AuthCallback {
        void onSuccess(UserRole role);
        void onError(String message);
    }

    private final FirebaseAuth mAuth;

    public AuthModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void login(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            UserRole role = UserRole.CHILD;
                            callback.onSuccess(role);
                        } else {
                            String msg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Login failed.";
                            callback.onError(msg);
                        }
                    }
                });
    }

    public void register(String email, String password, UserRole role, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(role);
                        } else {
                            Exception e = task.getException();
                            if(e != null) {
                                e.printStackTrace();   // 在 Logcat 打印完整错误
                                callback.onError(e.toString());  // 返回完整信息
                            } else {
                                callback.onError("Unknown error during login");
                            }
                            String msg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Sign up failed.";
                            callback.onError(msg);
                        }
                    }
                });
    }
}
