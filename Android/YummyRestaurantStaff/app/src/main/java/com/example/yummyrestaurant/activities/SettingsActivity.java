package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.appcompat.widget.SwitchCompat;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.MainActivity;
import com.example.yummyrestaurant.utils.LanguageManager;

public class SettingsActivity extends ThemeBaseActivity {

    private SwitchCompat notificationsSwitch;
    private Spinner languageSpinner;

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String KEY_NOTIFICATIONS = "enable_notifications";
    private boolean ignoreInitialLanguageSelection = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationsSwitch = findViewById(R.id.switch_notifications);
        languageSpinner = findViewById(R.id.spinner_language);

        // Restore states
        boolean notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS, false);
        notificationsSwitch.setChecked(notificationsEnabled);

        // Setup language spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.language_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        String savedLanguage = LanguageManager.getCurrentLanguage(this);
        languageSpinner.setSelection(LanguageManager.getLanguageSelectionIndex(savedLanguage));

        // Listeners
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            Toast.makeText(
                    this,
                    isChecked ? getString(R.string.notifications_enabled) : getString(R.string.notifications_disabled),
                    Toast.LENGTH_SHORT
            ).show();
        });

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCode = LanguageManager.getLanguageCodeByIndex(position);
                String currentCode = LanguageManager.getCurrentLanguage(SettingsActivity.this);

                if (ignoreInitialLanguageSelection) {
                    ignoreInitialLanguageSelection = false;
                    return;
                }

                if (selectedCode.equals(currentCode)) {
                    return;
                }

                LanguageManager.setCurrentLanguage(SettingsActivity.this, selectedCode);
                Toast.makeText(SettingsActivity.this, getString(R.string.language_updated), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
