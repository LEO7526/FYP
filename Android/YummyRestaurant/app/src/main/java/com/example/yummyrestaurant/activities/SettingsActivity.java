package com.example.yummyrestaurant.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConfig;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CouponListResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private SwitchCompat notificationsSwitch;
    private SwitchCompat darkModeSwitch;
    private Spinner apiEnvSpinner;

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String KEY_NOTIFICATIONS = "enable_notifications";
    private static final String KEY_DARK_MODE = "enable_dark_mode";
    private static final String KEY_API_ENV = "api_environment";

    private static final String[] API_OPTIONS = {"Emulator", "Phone"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkModeEnabled = preferences.getBoolean(KEY_DARK_MODE, false);

        AppCompatDelegate.setDefaultNightMode(
                darkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationsSwitch = findViewById(R.id.switch_notifications);
        darkModeSwitch = findViewById(R.id.switch_dark_mode);
        apiEnvSpinner = findViewById(R.id.spinner_api_env);

        // Restore states
        boolean notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS, false);
        notificationsSwitch.setChecked(notificationsEnabled);
        darkModeSwitch.setChecked(darkModeEnabled);

        // Setup API environment spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, API_OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        apiEnvSpinner.setAdapter(adapter);

        String savedEnv = preferences.getString(KEY_API_ENV, "Emulator");
        int selectedIndex = savedEnv.equals("Phone") ? 1 : 0;
        apiEnvSpinner.setSelection(selectedIndex);

        // Listeners
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled", Toast.LENGTH_SHORT).show();
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            Toast.makeText(this, isChecked ? "Dark mode on" : "Dark mode off", Toast.LENGTH_SHORT).show();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate();
        });

        apiEnvSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choice = API_OPTIONS[position];
                ApiConfig.setApiEnv(SettingsActivity.this, choice);

                // Reset Retrofit so next call uses new base URL
                RetrofitClient.reset();

                // Optional: immediately test the new environment
                CouponApiService service = RetrofitClient.getClient(SettingsActivity.this)
                        .create(CouponApiService.class);

                service.getCoupons().enqueue(new Callback<CouponListResponse>() {
                    @Override
                    public void onResponse(Call<CouponListResponse> call, Response<CouponListResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this,
                                    "API switched to " + choice + " and is working",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingsActivity.this,
                                    "API switched to " + choice + " but test failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CouponListResponse> call, Throwable t) {
                        Toast.makeText(SettingsActivity.this,
                                "API switch failed: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}