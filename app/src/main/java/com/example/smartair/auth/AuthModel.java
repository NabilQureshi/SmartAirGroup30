package com.example.smartair.auth;

import com.example.smartair.models.UserRole;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthModel implements LoginContract.Model {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthModel() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void login(String email, String password, LoginContract.AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        fetchUserRole(user.getUid(), callback);
                    } else {
                        callback.onFailure("Authentication failed");
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMessage = parseAuthError(e.getMessage());
                    callback.onFailure(errorMessage);
                });
    }

    private void fetchUserRole(String uid, LoginContract.AuthCallback callback) {
        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String roleString = documentSnapshot.getString("role");
                        if (roleString != null) {
                            try {
                                UserRole role = UserRole.fromString(roleString);
                                callback.onSuccess(uid, role);
                            } catch (IllegalArgumentException e) {
                                callback.onFailure("Invalid user role");
                            }
                        } else {
                            callback.onFailure("User role not found");
                        }
                    } else {
                        callback.onFailure("User data not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Failed to fetch user data"));
    }

    private String parseAuthError(String errorMessage) {
        if (errorMessage == null) {
            return "Authentication failed";
        }
        if (errorMessage.contains("no user record")) {
            return "No account found with this email";
        } else if (errorMessage.contains("password is invalid")) {
            return "Incorrect password";
        } else if (errorMessage.contains("network error")) {
            return "Network error. Please check your connection";
        }
        return "Authentication failed. Please try again";
    }
}