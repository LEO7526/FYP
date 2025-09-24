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
import com.example.yummyrestaurant.utils.RoleManager;
import com.example.yummyrestaurant.api.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import com.example.yummyrestaurant.api.RetrofitClient;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private LoginStaffApi loginStaffApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("enable_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_login);

        // Initialize UI components
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginBtn);

        Retrofit retrofit = RetrofitClient.getClient();
        loginStaffApi = retrofit.create(LoginStaffApi.class);

        // Set click listener for the login button
        loginButton.setOnClickListener(v -> {
            loginUser();
        });

        // Register link
        TextView registerLink = findViewById(R.id.registerLink);
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
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
            Log.d("LoginActivity", "Calling staff login API...");
            staffCall.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    Log.d("LoginActivity", "Staff login API response received.");
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.d("LoginActivity", "Staff login successful.");
                        handleLoginSuccess(response.body(), email);
                    } else {
                        Log.d("LoginActivity", "Staff login failed or user not found. Trying customer login...");

                        Call<LoginResponse> customerCall = loginCustomerApi.loginUser(email, password);
                        customerCall.enqueue(new Callback<LoginResponse>() {
                            @Override
                            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                                Log.d("LoginActivity", "Customer login API response received.");
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    Log.d("LoginActivity", "Customer login successful.");
                                    handleLoginSuccess(response.body(), email);
                                } else {
                                    Log.d("LoginActivity", "Customer login failed.");
                                    Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<LoginResponse> call, Throwable t) {
                                Log.e("LoginActivity", "Customer login API call failed: " + t.getMessage());
                                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Log.e("LoginActivity", "Staff login API call failed: " + t.getMessage());
                    Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.w("LoginActivity", "Login attempt with empty email or password.");
            Toast.makeText(this, "Please enter your email and password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLoginSuccess(LoginResponse loginResponse, String email) {
        Log.i("LoginActivity", "Login successful for role: " + loginResponse.getRole());
        Log.i("LoginActivity", "User Info → ID: " + loginResponse.getUserId() +
                ", Name: " + loginResponse.getUserName() +
                ", Email: " + email +
                ", Role: " + loginResponse.getRole() +
                ", ImageUrl: " + loginResponse.getUserImageUrl() +
                ", Telephone: " + loginResponse.getUserTel()

        );

        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

        RoleManager.setUserEmail(email);
        RoleManager.setUserRole(loginResponse.getRole());
        RoleManager.setUserName(loginResponse.getUserName());
        RoleManager.setUserId(loginResponse.getUserId());
        RoleManager.setUserTel(loginResponse.getUserTel());
        RoleManager.setUserImageUrl(loginResponse.getUserImageUrl());

        if ("staff".equals(loginResponse.getRole())) {
            Log.d("LoginActivity", "Routing to DashboardActivity...");
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
        } else {
            Log.d("LoginActivity", "Routing to ProductListActivity...");
            startActivity(new Intent(LoginActivity.this, CustomerHomeActivity.class));
        }
        finish();
    }
}