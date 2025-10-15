package com.example.yummyrestaurant;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AppCompatDelegate;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.activities.BrowseMenuActivity;
import com.example.yummyrestaurant.activities.RegisterActivity;
import com.example.yummyrestaurant.api.LoginCustomerApi;
import com.example.yummyrestaurant.api.LoginStaffApi;
import com.example.yummyrestaurant.api.LoginResponse;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Customization;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginBottomSheetFragment extends BottomSheetDialogFragment {

    public interface LoginListener {
        void onLoginResult(boolean success);
    }

    private LoginListener loginListener;

    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Reuse your existing activity_login.xml layout
        View view = inflater.inflate(R.layout.activity_login, container, false);

        // Apply dark mode preference if needed
        SharedPreferences prefs = requireContext().getSharedPreferences("AppSettingsPrefs", requireContext().MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("enable_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Initialize UI components
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        loginButton = view.findViewById(R.id.loginBtn);

        Retrofit retrofit = RetrofitClient.getClient(requireContext());
        LoginStaffApi loginStaffApi = retrofit.create(LoginStaffApi.class);

        // Set click listener for the login button
        loginButton.setOnClickListener(v -> loginUser());

        // Register link
        TextView registerLink = view.findViewById(R.id.registerLink);
        registerLink.setOnClickListener(v -> {
            dismiss(); // close the login sheet first
            RegisterBottomSheetFragment registerSheet = new RegisterBottomSheetFragment();
            registerSheet.show(getParentFragmentManager(), "register_sheet");
        });

        return view;
    }

    // Method to login the user
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.isEmpty() && !password.isEmpty()) {
            Log.d("LoginBottomSheet", "Attempting login with email: " + email);

            // ✅ Pass context into getClient()
            Retrofit retrofit = RetrofitClient.getClient(requireContext());
            LoginStaffApi loginStaffApi = retrofit.create(LoginStaffApi.class);
            LoginCustomerApi loginCustomerApi = retrofit.create(LoginCustomerApi.class);

            Call<LoginResponse> staffCall = loginStaffApi.loginUser(email, password);
            staffCall.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        handleLoginSuccess(response.body(), email);
                    } else {
                        // Try customer login if staff login fails
                        Call<LoginResponse> customerCall = loginCustomerApi.loginUser(email, password);
                        customerCall.enqueue(new Callback<LoginResponse>() {
                            @Override
                            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    handleLoginSuccess(response.body(), email);
                                } else {
                                    Toast.makeText(getContext(),
                                            "Login failed. Please check your credentials.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<LoginResponse> call, Throwable t) {
                                Toast.makeText(getContext(),
                                        "Network error: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(getContext(),
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Please enter your email and password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLoginSuccess(LoginResponse loginResponse, String email) {
        Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();

        // Save user info
        // Save user info
        RoleManager.setUserEmail(email);
        RoleManager.setUserRole(loginResponse.getRole());
        Log.d("LoginBottomSheetFragment", "Role = " + RoleManager.getUserRole());
        RoleManager.setUserName(loginResponse.getUserName());
        RoleManager.setUserId(loginResponse.getUserId());
        RoleManager.setUserTel(loginResponse.getUserTel());

        // 🔑 Add this log BEFORE setting image URL
        Log.d("LoginBottomSheetFragment",
                "API returned userImageUrl = " + loginResponse.getUserImageUrl());

        RoleManager.setUserImageUrl(loginResponse.getUserImageUrl());

        // 🔑 Add this log AFTER setting image URL
        Log.d("LoginBottomSheetFragment",
                "RoleManager stored userImageUrl = " + RoleManager.getUserImageUrl());




        // Restore pending cart item if present
        Bundle args = getArguments();
        if (args != null) {
            MenuItem pendingItem = (MenuItem) args.getSerializable("pendingMenuItem");
            int qty = args.getInt("pendingQuantity", 0);
            String spice = args.getString("pendingSpice");
            String notes = args.getString("pendingNotes");

            if (pendingItem != null && qty > 0) {
                Customization customization = null;
                boolean hasSpice = spice != null && !spice.isEmpty();
                boolean hasNotes = notes != null && !notes.isEmpty();
                if (hasSpice || hasNotes) {
                    customization = new Customization(hasSpice ? spice : null, hasNotes ? notes : null);
                }

                CartItem cartItem = new CartItem(pendingItem, customization);
                Integer existing = CartManager.getItemQuantity(cartItem);
                int base = (existing != null) ? existing : 0;
                CartManager.updateQuantity(cartItem, base + qty);


                Log.d("CartDebug", "Restored pending item: stableId="
                        + pendingItem.hashCode()
                        + " customization=" + customization + " qty=" + qty);

                Toast.makeText(
                        getContext(),
                        qty + " × " + (pendingItem.getName() == null ? "" : pendingItem.getName()) +
                                (customization != null && customization.getSpiceLevel() != null
                                        ? " (" + customization.getSpiceLevel() + ")"
                                        : "") +
                                " added to cart",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        // set login to true
        BrowseMenuActivity.setLogin(true);

        // Notify parent activity
        if (loginListener != null) {
            loginListener.onLoginResult(true);
        }

        dismiss();
    }
}