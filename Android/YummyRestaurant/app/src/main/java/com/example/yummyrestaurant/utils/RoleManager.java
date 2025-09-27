package com.example.yummyrestaurant.utils;

import android.util.Log;

import com.example.yummyrestaurant.models.User;

public class RoleManager {

    public static User getUser() {
        if (userId == null || userEmail == null || userName == null || userTel == null) {
            return null; // Not logged in or incomplete data
        }
        return new User(userId, userName, userEmail, userTel);
    }
    private static final String TAG = "RoleManager";

    private static String userId;
    private static String userEmail;
    private static String userRole;
    private static String userName;
    private static String userTel;
    private static Integer assignedTableNumber; // For staff use
    private static String userImageUrl;

    public static String getUserId() {
        Log.d(TAG, "getUserId: " + userId);
        return userId;
    }

    public static void setUserId(String userId) {
        Log.d(TAG, "setUserId: " + userId);
        RoleManager.userId = userId;
    }

    public static String getUserEmail() {
        Log.d(TAG, "getUserEmail: " + userEmail);
        return userEmail;
    }

    public static void setUserEmail(String userEmail) {
        Log.d(TAG, "setUserEmail: " + userEmail);
        RoleManager.userEmail = userEmail;
    }

    public static String getUserRole() {
        Log.d(TAG, "getUserRole: " + userRole);
        return userRole;
    }

    public static void setUserRole(String userRole) {
        Log.d(TAG, "setUserRole: " + userRole);
        RoleManager.userRole = userRole;
    }

    public static String getUserName() {
        Log.d(TAG, "getUserName: " + userName);
        return userName;
    }

    public static void setUserName(String userName) {
        Log.d(TAG, "setUserName: " + userName);
        RoleManager.userName = userName;
    }

    public static String getUserTel() {
        Log.d(TAG, "getUserTel: " + userTel);
        return userTel;
    }

    public static void setUserTel(String userTel) {
        Log.d(TAG, "setUserTel: " + userTel);
        RoleManager.userTel = userTel;
    }

    public static void clearUserData() {
        Log.d(TAG, "clearUserData: Resetting all user fields");
        userId = null;
        userEmail = null;
        userRole = null;
        userName = null;
        userTel = null;
        assignedTableNumber = null;
        userImageUrl = null;
    }

    public static boolean isStaff() {
        boolean result = "staff".equalsIgnoreCase(userRole);
        Log.d(TAG, "isStaff: " + result);
        return result;
    }

    public static void setAssignedTable(int tableNumber) {
        Log.d(TAG, "setAssignedTable: " + tableNumber);
        RoleManager.assignedTableNumber = tableNumber;
    }

    public static Integer getAssignedTable() {
        Log.d(TAG, "getAssignedTable: " + assignedTableNumber);
        return assignedTableNumber;
    }

    public static boolean hasAssignedTable() {
        boolean result = assignedTableNumber != null;
        Log.d(TAG, "hasAssignedTable: " + result);
        return result;
    }

    public static String getUserImageUrl() {
        String role = getUserRole();
        String resolvedPath;

        if ("staff".equals(role)) {
            resolvedPath = (userImageUrl != null && !userImageUrl.isEmpty()) ? "uploads/staff/" + userImageUrl : null;
        } else {
            resolvedPath = (userImageUrl != null && !userImageUrl.isEmpty()) ? "uploads/customers/" + userImageUrl : null;
        }

        Log.d(TAG, "getUserImageUrl: role=" + role + ", resolvedPath=" + resolvedPath);
        return resolvedPath;
    }

    public static void setUserImageUrl(String userImageUrl) {
        Log.d(TAG, "setUserImageUrl: " + userImageUrl);
        RoleManager.userImageUrl = userImageUrl;
    }
}