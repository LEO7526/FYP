package com.example.yummyrestaurant;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yummyrestaurant.api.RegisterApi;
import com.example.yummyrestaurant.api.RegisterResponse;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterBottomSheetFragment extends BottomSheetDialogFragment {

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginLink;
    private RegisterApi registerApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_register, container, false);

        // Initialize UI components
        nameEditText = view.findViewById(R.id.name);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        confirmPasswordEditText = view.findViewById(R.id.confirmPassword);
        registerButton = view.findViewById(R.id.registerBtn);
        loginLink = view.findViewById(R.id.loginLink);

        // Retrofit setup
        Retrofit retrofit = RetrofitClient.getClient(requireContext());
        registerApi = retrofit.create(RegisterApi.class);

        // Register button click
        registerButton.setOnClickListener(v -> registerUser());

        // Inline switch back to login sheet
        loginLink.setOnClickListener(v -> {
            dismiss();
            LoginBottomSheetFragment loginSheet = new LoginBottomSheetFragment();
            loginSheet.show(getParentFragmentManager(), "login_sheet");
        });

        return view;
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!isValidInput(name, email, password, confirmPassword)) {
            return;
        }

        Log.d("RegisterDebug", "Sending data: " + name + ", " + email);

        Call<RegisterResponse> call = registerApi.registerUser(name, email, password);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                Log.d("RegisterDebug", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    Log.d("RegisterDebug", "Response body: " + registerResponse.getMessage());

                    if (registerResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                        dismiss();

                        // Auto-open login sheet after successful registration
                        LoginBottomSheetFragment loginSheet = new LoginBottomSheetFragment();
                        loginSheet.show(getParentFragmentManager(), "login_sheet");
                    } else {
                        Toast.makeText(getContext(), registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Server error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e("RegisterDebug", "Network failure", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            nameEditText.setError("Please fill in your name");
            nameEditText.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Please fill in your email address");
            emailEditText.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format");
            emailEditText.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Please fill in your password");
            passwordEditText.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Please fill in your confirm password");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }
}