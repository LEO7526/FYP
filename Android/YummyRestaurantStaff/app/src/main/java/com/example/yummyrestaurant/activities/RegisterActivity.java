package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yummyrestaurant.R;
import android.widget.TextView;
import com.example.yummyrestaurant.api.RegisterApi;
import com.example.yummyrestaurant.api.RegisterResponse;
import com.example.yummyrestaurant.api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends ThemeBaseActivity {

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginLink;
    private RegisterApi registerApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.registerBtn);
        loginLink = findViewById(R.id.loginLink);

        Retrofit retrofit = RetrofitClient.getClient(this);
        registerApi = retrofit.create(RegisterApi.class);

        registerButton.setOnClickListener(v -> {
            registerUser();
        });

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!isValidInput(name, email, password, confirmPassword)) {
            return;
        }

        Log.d("RegisterDebug", "Sending data: " + name + ", " + email+ ", " + password);

        Call<RegisterResponse> call = registerApi.registerUser(name, email, password);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                Log.d("RegisterDebug", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    Log.d("RegisterDebug", "Response body: " + registerResponse.getMessage());

                    if (registerResponse.isSuccess()) {
                        Toast.makeText(RegisterActivity.this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("RegisterDebug", "Error body: " + errorBody);
                        Toast.makeText(RegisterActivity.this, getString(R.string.server_error_with_reason, errorBody), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("RegisterDebug", "Error parsing error body", e);
                        Toast.makeText(RegisterActivity.this, getString(R.string.registration_failed_try_again), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e("RegisterDebug", "Network failure", t);
                Toast.makeText(RegisterActivity.this, getString(R.string.network_error_with_reason, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            nameEditText.setError(getString(R.string.please_fill_name));
            nameEditText.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            emailEditText.setError(getString(R.string.please_fill_email));
            emailEditText.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError(getString(R.string.please_fill_password));
            passwordEditText.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError(getString(R.string.please_fill_confirm_password));
            confirmPasswordEditText.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.passwords_do_not_match));
            confirmPasswordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError(getString(R.string.password_min_6_chars));
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }
}
