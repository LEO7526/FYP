package com.example.yummyrestaurant.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.utils.LanguageManager;

/**
 * Base Activity for staff screens that need locale support.
 * All staff activities extending AppCompatActivity directly should extend this instead.
 */
public abstract class StaffBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApiConstants.refresh();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.applyLocale(newBase));
    }
}
