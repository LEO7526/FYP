package com.example.yummyrestaurant.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.yummyrestaurant.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private SwitchCompat notificationsSwitch;
    private SwitchCompat darkModeSwitch;

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String KEY_NOTIFICATIONS = "enable_notifications";
    private static final String KEY_DARK_MODE = "enable_dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starting SettingsActivity");

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkModeEnabled = preferences.getBoolean(KEY_DARK_MODE, false);
        Log.d(TAG, "onCreate: Dark mode preference = " + darkModeEnabled);

        AppCompatDelegate.setDefaultNightMode(
                darkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: After super.onCreate");

        setContentView(R.layout.activity_settings);
        Log.d(TAG, "onCreate: Layout set");

        notificationsSwitch = findViewById(R.id.switch_notifications);
        darkModeSwitch = findViewById(R.id.switch_dark_mode);
        Log.d(TAG, "onCreate: Switches bound");

        boolean notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS, false);
        notificationsSwitch.setChecked(notificationsEnabled);
        darkModeSwitch.setChecked(darkModeEnabled);
        Log.d(TAG, "onCreate: Switch states restored");

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "notificationsSwitch toggled: " + isChecked);
            preferences.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled", Toast.LENGTH_SHORT).show();
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "darkModeSwitch toggled: " + isChecked);
            boolean currentModeIsDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
            boolean shouldSwitch = isChecked != currentModeIsDark;

            preferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            Toast.makeText(this, isChecked ? "Dark mode on" : "Dark mode off", Toast.LENGTH_SHORT).show();

            if (shouldSwitch) {
                Log.d(TAG, "Theme change required. Recreating activity...");
                AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
                recreate();
            } else {
                Log.d(TAG, "Theme already applied. No need to recreate.");
            }
        });

        Log.d(TAG, "onCreate: Setup complete");
    }
}