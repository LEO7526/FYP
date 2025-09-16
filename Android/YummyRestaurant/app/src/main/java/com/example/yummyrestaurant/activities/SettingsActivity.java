package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch notificationsSwitch;
    private Switch darkModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationsSwitch = findViewById(R.id.switch_notifications);
        darkModeSwitch = findViewById(R.id.switch_dark_mode);

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ? "Notifications enabled" : "Notifications disabled";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ? "Dark mode on" : "Dark mode off";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }
}