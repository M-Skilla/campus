package com.group.campus.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREFS_NAME = "campus_connect_prefs";
    private static final String KEY_IS_FIRST_TIME = "is_first_time";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    public boolean isFirstTime() {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_TIME, true);
    }


    public void setFirstTimeComplete() {
        editor.putBoolean(KEY_IS_FIRST_TIME, false);
        editor.apply();
    }


//    public boolean isUserLoggedIn() {
//        return sharedPreferences.getBoolean(KEY_USER_LOGGED_IN, false);
//    }
//
//    /**
//     * Set user login status
//     * @param isLoggedIn true if user is logged in, false otherwise
//     */
//    public void setUserLoggedIn(boolean isLoggedIn) {
//        editor.putBoolean(KEY_USER_LOGGED_IN, isLoggedIn);
//        editor.apply();
//    }

    public void clearPreferences() {
        editor.clear();
        editor.apply();
    }
}
