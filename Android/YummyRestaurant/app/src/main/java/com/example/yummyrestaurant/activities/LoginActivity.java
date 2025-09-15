package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.utils.RoleManager;
import com.example.yummyrestaurant.api.LoginApi;
import com.example.yummyrestaurant.api.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import com.example.yummyrestaurant.api.RetrofitClient;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Spinner roleSpinner;
    private Button loginButton;
    private LoginApi loginApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginBtn);
        roleSpinner = findViewById(R.id.roleSpinner);

        Retrofit retrofit = RetrofitClient.getClient();
        loginApi = retrofit.create(LoginApi.class);

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
        String role = roleSpinner.getSelectedItem().toString().toLowerCase(); // "staff" or "customer"

        if (!email.isEmpty() && !password.isEmpty()) {
            Call<LoginResponse> call = loginApi.loginUser(email, password, role);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse loginResponse = response.body();
                        if (loginResponse.isSuccess()) {
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // Save user info
                            RoleManager.setUserEmail(email);
                            RoleManager.setUserRole(loginResponse.getRole());
                            RoleManager.setUserName(loginResponse.getUserName());
                            RoleManager.setUserId(loginResponse.getUserId());

                            // Log user info for debugging
                            Log.d("LoginActivity", "User logged in: " +
                                    "ID=" + RoleManager.getUserId() +
                                    ", Name=" + RoleManager.getUserName() +
                                    ", Email=" + RoleManager.getUserEmail() +
                                    ", Role=" + RoleManager.getUserRole());


                            // Route based on role
                            if ("staff".equals(loginResponse.getRole())) {
                                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, ProductListActivity.class));
                            }
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Please enter your email and password.", Toast.LENGTH_SHORT).show();
        }
    }
}