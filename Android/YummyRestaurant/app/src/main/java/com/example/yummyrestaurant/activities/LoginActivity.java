package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.LoginCustomerApi;
import com.example.yummyrestaurant.api.LoginStaffApi;
import com.example.yummyrestaurant.api.LoginResponse;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Customization;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private LoginStaffApi loginStaffApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("enable_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        setContentView(R.layout.activity_login);

        // Initialize UI components
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginBtn);

        Retrofit retrofit = RetrofitClient.getClient();
        loginStaffApi = retrofit.create(LoginStaffApi.class);

        // Set click listener for the login button
        loginButton.setOnClickListener(v -> loginUser());

        // Register link
        TextView registerLink = findViewById(R.id.registerLink);
        registerLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    // Method to login the user
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.isEmpty() && !password.isEmpty()) {
            Log.d("LoginActivity", "Attempting login with email: " + email);

            Retrofit retrofit = RetrofitClient.getClient();
            LoginStaffApi loginStaffApi = retrofit.create(LoginStaffApi.class);
            LoginCustomerApi loginCustomerApi = retrofit.create(LoginCustomerApi.class);

            Call<LoginResponse> staffCall = loginStaffApi.loginUser(email, password);
            staffCall.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        handleLoginSuccess(response.body(), email);
                    } else {
                        // Try customer login
                        Call<LoginResponse> customerCall = loginCustomerApi.loginUser(email, password);
                        customerCall.enqueue(new Callback<LoginResponse>() {
                            @Override
                            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    handleLoginSuccess(response.body(), email);
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            "Login failed. Please check your credentials.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<LoginResponse> call, Throwable t) {
                                Toast.makeText(LoginActivity.this,
                                        "Network error: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this,
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Please enter your email and password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLoginSuccess(LoginResponse loginResponse, String email) {
        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

        // Save user info
        RoleManager.setUserEmail(email);
        RoleManager.setUserRole(loginResponse.getRole());
        RoleManager.setUserName(loginResponse.getUserName());
        RoleManager.setUserId(loginResponse.getUserId());
        RoleManager.setUserTel(loginResponse.getUserTel());
        RoleManager.setUserImageUrl(loginResponse.getUserImageUrl());

        // ✅ Restore pending cart item if present
        Intent data = getIntent();
        MenuItem pendingItem = (MenuItem) data.getSerializableExtra("pendingMenuItem");
        int qty = data.getIntExtra("pendingQuantity", 0);
        String spice = data.getStringExtra("pendingSpice");
        String notes = data.getStringExtra("pendingNotes");

        if (pendingItem != null && qty > 0) {
            Customization customization = new Customization(spice, notes);
            CartItem cartItem = new CartItem(pendingItem, customization);
            int currentQty = CartManager.getItemQuantity(cartItem);
            CartManager.updateQuantity(cartItem, currentQty + qty);

            Toast.makeText(
                    this,
                    qty + " × " + pendingItem.getName() +
                            (customization != null && customization.getSpiceLevel() != null
                                    ? " (" + customization.getSpiceLevel() + ")"
                                    : "") +
                            " added to cart",
                    Toast.LENGTH_SHORT
            ).show();

            // ✅ Clear extras so they won’t be reused
            data.removeExtra("pendingMenuItem");
            data.removeExtra("pendingQuantity");
            data.removeExtra("pendingSpice");
            data.removeExtra("pendingNotes");
        }

        // ✅ Redirect to CartActivity so user sees updated cart
        Intent intent = new Intent(LoginActivity.this, CartActivity.class);
        startActivity(intent);
        finish();
    }
}