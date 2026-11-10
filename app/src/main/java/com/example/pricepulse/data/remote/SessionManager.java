package com.example.pricepulse.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user authentication tokens and session state.
 */
public class SessionManager {
    private static final String PREF_NAME = "pricepulse_session";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USERNAME = "user_username";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_CREATED_AT = "user_created_at";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(String token) {
        preferences.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public String getAuthToken() {
        return preferences.getString(KEY_AUTH_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getAuthToken() != null;
    }

    public void saveUserProfile(String name, String email) {
        preferences.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public void saveFullProfile(long id, String username, String email, String fullName, String phone, String role, String createdAt) {
        preferences.edit()
                .putLong(KEY_USER_ID, id)
                .putString(KEY_USERNAME, username)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, fullName)
                .putString(KEY_USER_PHONE, phone)
                .putString(KEY_USER_ROLE, role)
                .putString(KEY_CREATED_AT, createdAt)
                .apply();
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, 0L);
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "PricePulse User");
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    public String getUserPhone() {
        return preferences.getString(KEY_USER_PHONE, "");
    }

    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, "user");
    }

    public String getCreatedAt() {
        return preferences.getString(KEY_CREATED_AT, "");
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getUserRole());
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}
