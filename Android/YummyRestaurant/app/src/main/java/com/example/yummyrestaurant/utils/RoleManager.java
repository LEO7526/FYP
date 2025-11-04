package com.example.yummyrestaurant.utils;

import android.util.Log;

import com.example.yummyrestaurant.models.User;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RoleManager {

    public static User getUser() {
        if (userId == null || userEmail == null || userName == null || userTel == null) {
            return null; // Not logged in or incomplete data
        }
        return new User(userId, userName, userEmail, userTel);
    }

    private static String userBirthday; // format: "yyyy-MM-dd"

    public static String getUserBirthday() {
        return userBirthday;
    }

    public static void setUserBirthday(String birthday) {
        userBirthday = birthday;
    }



    public static boolean isTodayUserBirthday() {
        if (userBirthday == null || userBirthday.isEmpty()) return false;

        try {
            // Parse stored birthday
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date dob = sdf.parse(userBirthday);

            Calendar today = Calendar.getInstance();
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(dob);

            // Compare month and day only
            return today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == birthCal.get(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse birthday: " + userBirthday, e);
            return false;
        }
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
        // Return the stored URL as-is, don't concatenate again
        return userImageUrl;
    }

    public static void setUserImageUrl(String imagePathOrFileName) {
        if (imagePathOrFileName == null || imagePathOrFileName.isEmpty()) {
            userImageUrl = null;
            return;
        }

        // ✅ If it's a full GitHub URL, store it directly
        if (imagePathOrFileName.startsWith("http://") || imagePathOrFileName.startsWith("https://")) {
            userImageUrl = imagePathOrFileName;
        }
        // ✅ If it's a raw filename, prepend local folder
        else if (!imagePathOrFileName.contains("Image/Profile_image")) {
            if ("staff".equals(userRole)) {
                userImageUrl = "../Image/Profile_image/Staff/" + imagePathOrFileName;
            } else {
                userImageUrl = "../Image/Profile_image/Customer/" + imagePathOrFileName;
            }
        }
        // ✅ Already a relative path
        else {
            userImageUrl = imagePathOrFileName;
        }

        Log.d(TAG, "setUserImageUrl: stored=" + userImageUrl);
    }

}