package com.example.smartair.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPrefsHelper {
    private static final String TAG = "SharedPrefsHelper";
    private static final String PREFS_NAME = "SmartAirPrefs";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PARENT_ID = "parent_id";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete_";

    private final SharedPreferences preferences;

    public SharedPrefsHelper(Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserRole(String role) {
        preferences.edit().putString(KEY_USER_ROLE, role).commit();
        Log.d(TAG, "Saved user role: " + role);
    }

    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, null);
    }

    public void saveUserUid(String uid) {
        preferences.edit().putString(KEY_USER_UID, uid).commit();
        Log.d(TAG, "Saved user UID: " + uid);
    }

    public String getUserUid() {
        return preferences.getString(KEY_USER_UID, null);
    }

    public void saveUserId(String userId) {
        preferences.edit().putString(KEY_USER_ID, userId).commit();
        Log.d(TAG, "Saved user ID: " + userId);
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public void saveParentId(String parentId) {
        preferences.edit().putString(KEY_PARENT_ID, parentId).commit();
        Log.d(TAG, "Saved parent ID: " + parentId);
    }

    public String getParentId() {
        return preferences.getString(KEY_PARENT_ID, null);
    }

    public void clear() {
        preferences.edit().clear().commit();
    }

    public void clearUserData() {
        String currentUserId = getUserId();
        preferences.edit()
                .remove(KEY_USER_ROLE)
                .remove(KEY_USER_UID)
                .remove(KEY_USER_ID)
                .remove(KEY_PARENT_ID)
                .remove(KEY_ONBOARDING_COMPLETE + currentUserId)
                .commit();
        Log.d(TAG, "Cleared user data for userId: " + currentUserId);
    }

    public void setOnboardingComplete(boolean complete) {
        String userId = getUserId();
        if (userId == null) {
            userId = getUserUid();
        }
        if (userId != null) {
            String key = KEY_ONBOARDING_COMPLETE + userId;
            preferences.edit().putBoolean(key, complete).commit();
            Log.d(TAG, "Set onboarding complete for userId " + userId + ": " + complete + " (key: " + key + ")");
        } else {
            Log.e(TAG, "Cannot set onboarding complete - userId is null!");
        }
    }

    public boolean isOnboardingComplete() {
        String userId = getUserId();
        if (userId == null) {
            userId = getUserUid();
        }
        if (userId != null) {
            String key = KEY_ONBOARDING_COMPLETE + userId;
            boolean result = preferences.getBoolean(key, false);
            Log.d(TAG, "Checking onboarding for userId " + userId + ": " + result + " (key: " + key + ")");
            return result;
        }
        Log.e(TAG, "Cannot check onboardinguserId is null! Returning false");
        return false;
    }

    public static void saveString(Context context, String key, String value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .commit();
    }

    public static String getString(Context context, String key) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(key, null);
    }
}