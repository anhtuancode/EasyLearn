package com.studying.easylearn.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "EasyLearnSession";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_DAILY_GOAL = "daily_goal_minutes";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(int userId, String email, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "Learner");
    }

    // Đồng bộ cùng key KEY_USER_NAME với createLoginSession
    public String getFullName() {
        return pref.getString(KEY_USER_NAME, "Tuấn");
    }

    // Đồng bộ cùng key KEY_USER_EMAIL với createLoginSession
    public String getEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public void updateFullName(String newName) {
        editor.putString(KEY_USER_NAME, newName);
        editor.apply();
    }

    public void setDailyGoalMinutes(int minutes) {
        editor.putInt(KEY_DAILY_GOAL, minutes);
        editor.apply();
    }

    public int getDailyGoalMinutes() {
        return pref.getInt(KEY_DAILY_GOAL, 3);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    // Gọi chung về hàm logout() để tránh lặp logic
    public void logoutUser() {
        logout();
    }
}