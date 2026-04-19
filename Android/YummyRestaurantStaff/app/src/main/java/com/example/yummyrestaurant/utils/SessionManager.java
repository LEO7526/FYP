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

        // Also update RoleManager for compatibility with main app
        RoleManager.setUserId(String.valueOf(sid));
        RoleManager.setUserName(name);
        RoleManager.setUserRole("staff");
    }

    public boolean isLoggedIn() {
        // Check both SessionManager and RoleManager
        boolean sessionLogin = pref.getBoolean(KEY_IS_LOGGED_IN, false);
        boolean roleManagerLogin = "staff".equalsIgnoreCase(RoleManager.getUserRole());
        return sessionLogin || roleManagerLogin;
    }

    public void logout() {
        editor.clear();
        editor.apply();
        
        // Also clear RoleManager
        RoleManager.clearUserData();
    }

    public String getStaffName() {
        String sessionName = pref.getString(KEY_NAME, null);
        if (sessionName != null) {
            return sessionName;
        }
        // Fallback to RoleManager
        return RoleManager.getUserName() != null ? RoleManager.getUserName() : "Staff";
    }
}