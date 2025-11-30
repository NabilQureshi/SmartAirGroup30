package com.example.smartair.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smartair.child_managent.AddChildActivity;

public class SharedPrefsHelper {
    private static final String PREFS_NAME = "SmartAirPrefs";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PARENT_ID = "parent_id";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private final SharedPreferences preferences;
    private static final String PREF_NAME = "SMARTAIR_PREFS";
    public SharedPrefsHelper(Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(key, value).apply();
    }

    // Get a stored string value
    public static String getString(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public void saveUserRole(String role) {
        preferences.edit().putString(KEY_USER_ROLE, role).apply();
    }

    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, null);
    }

    public void saveUserUid(String uid) {
        preferences.edit().putString(KEY_USER_UID, uid).apply();
    }

    public String getUserUid() {
        return preferences.getString(KEY_USER_UID, null);
    }

    public void saveUserId(String userId) {
        preferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public void saveParentId(String parentId) {
        preferences.edit().putString(KEY_PARENT_ID, parentId).apply();
    }

    public String getParentId() {
        return preferences.getString(KEY_PARENT_ID, null);
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    public void clearUserData() {
        boolean onboardingStatus = isOnboardingComplete();
        preferences.edit()
                .remove(KEY_USER_ROLE)
                .remove(KEY_USER_UID)
                .remove(KEY_USER_ID)
                .remove(KEY_PARENT_ID)
                .apply();
    }

    public void setOnboardingComplete(boolean complete) {
        preferences.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply();
    }

    public boolean isOnboardingComplete() {
        return preferences.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }
}