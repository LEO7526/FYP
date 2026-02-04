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
import com.example.yummyrestaurant.activities.StaffOrdersActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends ThemeBaseActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private LoginStaffApi loginStaffApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize RoleManager
        RoleManager.init(this);

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

        Retrofit retrofit = RetrofitClient.getClient(this);
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

            Retrofit retrofit = RetrofitClient.getClient(this);
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

        // Initialize RoleManager first
        RoleManager.init(this);
        
        android.util.Log.d("LoginActivity", "=== LOGIN SUCCESS ===");
        android.util.Log.d("LoginActivity", "Email: " + email);
        android.util.Log.d("LoginActivity", "Response Role: " + loginResponse.getRole());
        android.util.Log.d("LoginActivity", "Response UserName: " + loginResponse.getUserName());
        android.util.Log.d("LoginActivity", "Response UserId: " + loginResponse.getUserId());

        // Save user info
        RoleManager.setUserEmail(email);
        RoleManager.setUserRole(loginResponse.getRole());
        RoleManager.setUserName(loginResponse.getUserName());
        RoleManager.setUserId(loginResponse.getUserId());
        RoleManager.setUserTel(loginResponse.getUserTel());
        RoleManager.setUserImageUrl(loginResponse.getUserImageUrl());
        
        // Verify the data was saved
        android.util.Log.d("LoginActivity", "=== VERIFICATION AFTER SAVE ===");
        android.util.Log.d("LoginActivity", "Saved Role: " + RoleManager.getUserRole());
        android.util.Log.d("LoginActivity", "Saved Name: " + RoleManager.getUserName());
        android.util.Log.d("LoginActivity", "Is Staff: " + RoleManager.isStaff());

        // Restore pending cart item if present
        Intent data = getIntent();
        if (data != null) {
            MenuItem pendingItem = (MenuItem) data.getSerializableExtra("pendingMenuItem");
            int qty = data.getIntExtra("pendingQuantity", 0);
            String spice = data.getStringExtra("pendingSpice");
            String notes = data.getStringExtra("pendingNotes");

            if (pendingItem != null && qty > 0) {
                // Only create customization object when at least one field is present
                Customization customization = null;
                boolean hasSpice = spice != null && !spice.isEmpty();
                boolean hasNotes = notes != null && !notes.isEmpty();
                if (hasSpice || hasNotes) {
                    customization = new Customization(hasSpice ? spice : null, hasNotes ? notes : null);
                }

                CartItem cartItem = new CartItem(pendingItem, customization);
                // Use addItem to aggregate quantities correctly
                CartManager.addItem(cartItem, qty);

                android.util.Log.d("CartDebug", "Restored pending item: stableId="
                        + (pendingItem != null ? pendingItem.hashCode() : "null")
                        + " customization=" + customization + " qty=" + qty);

                Toast.makeText(
                        this,
                        qty + " × " + (pendingItem.getName() == null ? "" : pendingItem.getName()) +
                                (customization != null && customization.getSpiceLevel() != null
                                        ? " (" + customization.getSpiceLevel() + ")"
                                        : "") +
                                " added to cart",
                        Toast.LENGTH_SHORT
                ).show();

                // Clear extras so they won’t be reused
                try {
                    data.removeExtra("pendingMenuItem");
                    data.removeExtra("pendingQuantity");
                    data.removeExtra("pendingSpice");
                    data.removeExtra("pendingNotes");
                } catch (Exception ignored) {}
            }
        }

        // set login to true
        BrowseMenuActivity.setLogin(true);

        // Redirect based on user role
        String userRole = RoleManager.getUserRole();
        Intent intent;
        
        android.util.Log.d("LoginActivity", "=== NAVIGATION DECISION ===");
        android.util.Log.d("LoginActivity", "Final Role Check: " + userRole);
        
        if ("staff".equals(userRole)) {
            android.util.Log.d("LoginActivity", "🍳 Redirecting STAFF to StaffOrdersActivity");
            intent = new Intent(LoginActivity.this, StaffOrdersActivity.class);
        } else {
            android.util.Log.d("LoginActivity", "🛒 Redirecting CUSTOMER to BrowseMenuActivity");
            intent = new Intent(LoginActivity.this, BrowseMenuActivity.class);
        }
        
        // Optionally clear back stack so user cannot go back to login
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
