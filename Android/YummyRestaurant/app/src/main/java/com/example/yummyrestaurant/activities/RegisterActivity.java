package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {

    // UI Components
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI components
        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.registerBtn);

        // Set click listener for the register button
        registerButton.setOnClickListener(v -> {
            registerUser();
        });
    }

    // Method to register the user
    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate fields
        if (!isValidInput(name, email, password, confirmPassword)) {
            return;
        }

        // Create user using Firebase Authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        // Optional: Store additional user information
                        saveUserData(name, email);
                        // Navigate to the next screen
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        // Registration failed
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(this, "Password is too weak. Please use a stronger password.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to validate user input
    private boolean isValidInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Confirm password is required");
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

    // Method to save additional user data (optional)
    private void saveUserData(String name, String email) {
        // You can save additional user data (e.g., name) to Firebase Realtime Database or Firestore
        // Example:
        // DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        // String userId = auth.getCurrentUser().getUid();
        // User user = new User(name, email);
        // usersRef.child(userId).setValue(user);

        Toast.makeText(this, "Additional user data saved", Toast.LENGTH_SHORT).show();
    }
}
