package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.utils.ThemeManager;
import android.util.Log;

/**
 * Base Activity class that automatically applies app-wide theme
 * All activities should extend this class to get automatic theming
 * 
 * Customers: Orange theme (#FF6F00)
 * Staff: Blue theme (#1976D2)
 */
public abstract class ThemeBaseActivity extends AppCompatActivity {

    private static final String TAG = "ThemeBaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate: ThemeBaseActivity initialized for " + this.getClass().getSimpleName());
        
        // ✅ Apply theme based on user role
        applyAppTheme();
    }

    /**
     * Apply theme to this activity
     * Can be overridden by subclasses for custom theming
     */
    protected void applyAppTheme() {
        // Log theme application
        ThemeManager.logThemeApplication(this.getClass().getSimpleName());
        
        Log.i(TAG, "Theme Manager applied: " + ThemeManager.getThemeName());
    }

    /**
     * Helper method to apply theme to a button
     */
    protected void applyThemeToButton(android.widget.Button button) {
        ThemeManager.applyThemeToButton(button, this);
    }

    /**
     * Helper method to apply theme to a TextView
     */
    protected void applyThemeToTextView(android.widget.TextView textView) {
        ThemeManager.applyThemeToTextView(textView, this);
    }

    /**
     * Helper method to apply theme to toolbar
     */
    protected void applyThemeToToolbar(androidx.appcompat.widget.Toolbar toolbar) {
        ThemeManager.applyThemeToToolbar(toolbar, this);
    }

    /**
     * Get the current theme color
     */
    protected int getThemeColor() {
        return ThemeManager.getPrimaryColor(this);
    }

    /**
     * Get the current theme name
     */
    protected String getThemeName() {
        return ThemeManager.getThemeName();
    }

    /**
     * Get the current theme color as hex string
     */
    protected String getThemeColorHex() {
        return ThemeManager.getColorHex(this);
    }
}
