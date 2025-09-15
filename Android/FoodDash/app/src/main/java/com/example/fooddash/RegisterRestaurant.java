package com.example.fooddash;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.fooddash.model.Restaurant;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterRestaurant extends AppCompatActivity {

    private DatabaseReference rest;
    private Restaurant restaurant;

    private EditText restName, restLogo, restAddress;
    private TimePicker restOpen, restClose;
    private Button registerRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_restaurant);

        // Firebase reference
        rest = FirebaseDatabase.getInstance().getReference("Restaurant");

        // UI elements
        restName = findViewById(R.id.editTextRestName);
        restLogo = findViewById(R.id.editTextRestLogo);
        restAddress = findViewById(R.id.editTextAddress);
        restOpen = findViewById(R.id.datePickerOpen);
        restClose = findViewById(R.id.datePickerClose);
        registerRest = findViewById(R.id.register_button);

        restOpen.setIs24HourView(true);
        restClose.setIs24HourView(true);

        registerRest.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                String name = restName.getText().toString().trim();
                String logoUrl = restLogo.getText().toString().trim();
                String address = restAddress.getText().toString().trim();

                // Validation
                if (TextUtils.isEmpty(name)) {
                    restName.setError("Missing name");
                    return;
                }

                if (TextUtils.isEmpty(logoUrl)) {
                    restLogo.setError("Missing logo");
                    return;
                }

                if (!Patterns.WEB_URL.matcher(logoUrl).matches()) {
                    restLogo.setError("Invalid URL");
                    return;
                }

                if (TextUtils.isEmpty(address)) {
                    restAddress.setError("Missing address");
                    return;
                }

                int openHour = restOpen.getHour();
                int closeHour = restClose.getHour();
                int openMinute = restOpen.getMinute();
                int closeMinute = restClose.getMinute();

                if (openHour > closeHour || (openHour == closeHour && openMinute >= closeMinute)) {
                    Toast.makeText(RegisterRestaurant.this, "Open time must be before close time", Toast.LENGTH_SHORT).show();
                    return;
                }

                String openTime = openHour + ":" + openMinute;
                String closeTime = closeHour + ":" + closeMinute;

                // Create restaurant object
                restaurant = new Restaurant(name, logoUrl, "", address, openTime, closeTime, 0.0, 0.0);

                // Save to Firebase
                rest.child("Active").push().setValue(restaurant)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(RegisterRestaurant.this, "Restaurant registered!", Toast.LENGTH_SHORT).show();
                            returnAdminPanel();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(RegisterRestaurant.this, "Failed to register restaurant", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void returnAdminPanel() {
        Intent intent = new Intent(this, AdminPanel.class);
        startActivity(intent);
    }
}