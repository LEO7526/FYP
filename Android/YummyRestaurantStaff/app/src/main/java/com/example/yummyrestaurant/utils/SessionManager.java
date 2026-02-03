package com.example.yummyrestaurant.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "StaffSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_SID = "sid";
    private static final String KEY_NAME = "name";
    private static final String KEY_ROLE = "role";

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(int sid, String name, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_SID, sid);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public String getStaffName() {
        return pref.getString(KEY_NAME, "Staff");
    }
}