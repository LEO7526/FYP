package com.example.yummyrestaurant.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.yummyrestaurant.database.DatabaseHelper;

public class RoleManager {
    private static String userId;
    private static String userEmail;
    private static String userRole;
    private static String userName;

    // 设置用户信息
    public static void setUserId(String userId) {
        RoleManager.userId = userId;
    }

    public static void setUserEmail(String userEmail) {
        RoleManager.userEmail = userEmail;
    }

    public static void setUserRole(String userRole) {
        RoleManager.userRole = userRole;
    }

    public static void setUserName(String userName) {
        RoleManager.userName = userName;
    }

    // 获取用户信息
    public static String getUserId() {
        return userId;
    }

    public static String getUserEmail() {
        return userEmail;
    }

    public static String getUserRole() {
        return userRole;
    }

    public static String getUserName() {
        return userName;
    }

    // 方法用于从SQLite获取用户角色
    public static void getUserRoleFromSQLite(Context context, SQLiteDatabase db, String userId, RoleCallback callback) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        String role = dbHelper.getUserRole(db, userId);
        callback.onRoleReceived(role);
    }

    // 回调接口用于处理角色接收
    public interface RoleCallback {
        void onRoleReceived(String role);
    }
}
