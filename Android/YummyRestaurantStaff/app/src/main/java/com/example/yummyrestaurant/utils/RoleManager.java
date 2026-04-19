package com.example.yummyrestaurant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.yummyrestaurant.models.User;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RoleManager {
    private static final String TAG = "RoleManager";
    private static final String PREF_NAME = "RoleManagerPrefs";
    
    // SharedPreferences keys
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_TEL = "userTel";
    private static final String KEY_USER_IMAGE_URL = "userImageUrl";
    private static final String KEY_USER_BIRTHDAY = "userBirthday";
    private static final String KEY_ASSIGNED_TABLE_NUMBER = "assignedTableNumber";
    
    private static Context context;
    
    // Initialize RoleManager with context
    public static void init(Context ctx) {
        context = ctx.getApplicationContext();
    }
    
    private static SharedPreferences getPrefs() {
        if (context == null) {
            Log.e(TAG, "RoleManager not initialized! Call RoleManager.init(context) first");
            return null;
        }
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static User getUser() {
        String userId = getUserId();
        String userEmail = getUserEmail();
        String userName = getUserName();
        String userTel = getUserTel();
        
        if (userId == null || userEmail == null || userName == null || userTel == null) {
            return null; // Not logged in or incomplete data
        }
        return new User(userId, userName, userEmail, userTel);
    }

    public static String getUserBirthday() {
        SharedPreferences prefs = getPrefs();
        return prefs != null ? prefs.getString(KEY_USER_BIRTHDAY, null) : null;
    }

    public static void setUserBirthday(String birthday) {
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            prefs.edit().putString(KEY_USER_BIRTHDAY, birthday).apply();
        }
    }

    public static boolean isTodayUserBirthday() {
        String userBirthday = getUserBirthday();
        if (userBirthday == null || userBirthday.isEmpty()) return false;

        try {
            // Parse MM-DD
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
            java.util.Date dob = sdf.parse(userBirthday);

            Calendar today = Calendar.getInstance();
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(dob);

            return today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == birthCal.get(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse birthday: " + userBirthday, e);
            return false;
        }
    }

    public static String getUserId() {
        SharedPreferences prefs = getPrefs();
        String userId = prefs != null ? prefs.getString(KEY_USER_ID, null) : null;
        Log.d(TAG, "getUserId: " + userId);
        return userId;
    }

    public static void setUserId(String userId) {
        Log.d(TAG, "setUserId: " + userId);
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            prefs.edit().putString(KEY_USER_ID, userId).apply();
        }
    }

    public static String getUserEmail() {
        SharedPreferences prefs = getPrefs();
        String userEmail = prefs != null ? prefs.getString(KEY_USER_EMAIL, null) : null;
        Log.d(TAG, "getUserEmail: " + userEmail);
        return userEmail;
    }

    public static void setUserEmail(String userEmail) {
        Log.d(TAG, "setUserEmail: " + userEmail);
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            prefs.edit().putString(KEY_USER_EMAIL, userEmail).apply();
        }
    }

    public static String getUserRole() {
        SharedPreferences prefs = getPrefs();
        String userRole = prefs != null ? prefs.getString(KEY_USER_ROLE, null) : null;
        Log.d(TAG, "getUserRole: " + userRole);
        return userRole;
    }

    public static void setUserRole(String userRole) {
        Log.d(TAG, "setUserRole: " + userRole);
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            prefs.edit().putString(KEY_USER_ROLE, userRole).apply();
        }
    }

    public static String getUserName() {
        SharedPreferences prefs = getPrefs();
        String userName = prefs != null ? prefs.getString(KEY_USER_NAME, null) : null;
        Log.d(TAG, "getUserName: " + userName);
        return userName;
    }

    public static void setUserName(String userName) {
        Log.d(TAG, "setUserName: " + userName);
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            prefs.edit().putString(KEY_USER_NAME, userName).apply();
        }
    }

    public static String getUserTel() {
        SharedPreferences prefs = getPrefs();
        String userTel = prefs != null ? prefs.getString(KEY_USER_TEL, null) : null;
        Log.d(TAG, "getUserTel: " + userTel);
        return userTel;
    }

    public static void setUserTel(String userTel) {
        Log.d(TAG, "setUserTel: " + userTel);
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            prefs.edit().putString(KEY_USER_TEL, userTel).apply();
        }
    }

    public static String getUserImageUrl() {
        SharedPreferences prefs = getPrefs();
        return prefs != null ? prefs.getString(KEY_USER_IMAGE_URL, null) : null;
    }

    public static void setUserImageUrl(String userImageUrl) {
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            prefs.edit().putString(KEY_USER_IMAGE_URL, userImageUrl).apply();
        }
    }

    public static Integer getAssignedTableNumber() {
        SharedPreferences prefs = getPrefs();
        if (prefs != null && prefs.contains(KEY_ASSIGNED_TABLE_NUMBER)) {
            return prefs.getInt(KEY_ASSIGNED_TABLE_NUMBER, -1);
        }
        return null;
    }

    public static void setAssignedTableNumber(Integer tableNumber) {
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            if (tableNumber != null) {
                prefs.edit().putInt(KEY_ASSIGNED_TABLE_NUMBER, tableNumber).apply();
            } else {
                prefs.edit().remove(KEY_ASSIGNED_TABLE_NUMBER).apply();
            }
        }
    }

    // Legacy method for compatibility
    public static void setAssignedTable(int tableNumber) {
        setAssignedTableNumber(tableNumber);
    }

    // Legacy method for compatibility
    public static Integer getAssignedTable() {
        return getAssignedTableNumber();
    }

    public static boolean isStaff() {
        String role = getUserRole();
        boolean result = "staff".equalsIgnoreCase(role);
        Log.d(TAG, "isStaff: " + result + " (role: " + role + ")");
        return result;
    }

    public static void clearUserData() {
        Log.d(TAG, "clearUserData: Resetting all user fields");
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
    }
}