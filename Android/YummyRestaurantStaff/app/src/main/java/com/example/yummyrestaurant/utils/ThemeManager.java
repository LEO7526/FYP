package com.example.yummyrestaurant.utils;

import android.content.Context;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Centralized Theme Manager for app-wide role-based theming
 * Customers: Orange (#FF6F00)
 * Staff: Blue (#1976D2)
 */
public class ThemeManager {

    // Color Constants
    private static final String COLOR_CUSTOMER = "#FF6F00";  // Orange
    private static final String COLOR_STAFF = "#1976D2";     // Material Blue

    private static final String TAG = "ThemeManager";

    /**
     * Get primary theme color based on user role
     */
    public static int getPrimaryColor(Context context) {
        if (RoleManager.isStaff()) {
            android.util.Log.d(TAG, "getPrimaryColor: Returning BLUE for staff");
            return Color.parseColor(COLOR_STAFF);
        }
        android.util.Log.d(TAG, "getPrimaryColor: Returning ORANGE for customer");
        return Color.parseColor(COLOR_CUSTOMER);
    }

    /**
     * Get theme color name for logging/display
     */
    public static String getThemeName() {
        if (RoleManager.isStaff()) {
            return "STAFF (Blue)";
        }
        return "CUSTOMER (Orange)";
    }

    /**
     * Apply theme to Button
     */
    public static void applyThemeToButton(Button button, Context context) {
        if (button != null) {
            int color = getPrimaryColor(context);
            button.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    /**
     * Apply theme to TextView (text color)
     */
    public static void applyThemeToTextView(TextView textView, Context context) {
        if (textView != null) {
            int color = getPrimaryColor(context);
            textView.setTextColor(color);
        }
    }

    /**
     * Apply theme to Toolbar
     */
    public static void applyThemeToToolbar(Toolbar toolbar, Context context) {
        if (toolbar != null) {
            int color = getPrimaryColor(context);
            toolbar.setBackgroundColor(color);
        }
    }

    /**
     * Apply theme to multiple buttons at once
     */
    public static void applyThemeToButtons(Context context, Button... buttons) {
        for (Button button : buttons) {
            applyThemeToButton(button, context);
        }
    }

    /**
     * Apply theme to multiple TextViews at once
     */
    public static void applyThemeToTextViews(Context context, TextView... textViews) {
        for (TextView textView : textViews) {
            applyThemeToTextView(textView, context);
        }
    }

    /**
     * Log theme application
     */
    public static void logThemeApplication(String activityName) {
        String theme = getThemeName();
        android.util.Log.i(TAG, ">>> Theme Applied to " + activityName + ": " + theme);
    }

    /**
     * Get color as hex string (for reference)
     */
    public static String getColorHex(Context context) {
        if (RoleManager.isStaff()) {
            return COLOR_STAFF;
        }
        return COLOR_CUSTOMER;
    }
}
