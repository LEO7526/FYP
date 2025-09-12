package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import retrofit2.converter.gson.GsonConverterFactory;


public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
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

        // 初始化 Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://github.com/LEO7526/FYP/blob/main/projectapi/") // 請替換為你的 API 網址
                .addConverterFactory(GsonConverterFactory.create())
                .build();
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

        if (!email.isEmpty() && !password.isEmpty()) {
            String role = "staff"; 

            Call<LoginResponse> call = loginApi.loginUser(email, password, role);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse loginResponse = response.body();
                        if (loginResponse.isSuccess()) {
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // 儲存角色資訊
                            RoleManager.setUserEmail(email);
                            RoleManager.setUserRole(loginResponse.getRole());

                            // 根據角色導頁
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
                        Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
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


