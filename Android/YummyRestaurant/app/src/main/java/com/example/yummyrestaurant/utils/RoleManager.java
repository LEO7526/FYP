package com.example.yummyrestaurant.utils;

public class RoleManager {
    private static String userId;
    private static String userEmail;
    private static String userRole;
    private static String userName;
    private static String userTel;

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        RoleManager.userId = userId;
    }

    public static String getUserEmail() {
        return userEmail;
    }

    public static void setUserEmail(String userEmail) {
        RoleManager.userEmail = userEmail;
    }

    public static String getUserRole() {
        return userRole;
    }

    public static void setUserRole(String userRole) {
        RoleManager.userRole = userRole;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        RoleManager.userName = userName;
    }

    public static String getUserTel() {
        return userTel;
    }

    public static void setUserTel(String userTel) {
        RoleManager.userTel = userTel;
    }

    public static void clearUserData() {
        userId = null;
        userEmail = null;
        userRole = null;
        userName = null;
        userTel = null;
    }

}
