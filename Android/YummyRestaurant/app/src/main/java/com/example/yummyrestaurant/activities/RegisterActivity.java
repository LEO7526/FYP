package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.database.DatabaseHelper;
import com.example.yummyrestaurant.utils.RoleManager;

public class RegisterActivity extends AppCompatActivity {

    // UI Components
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

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

        // Save user data to SQLite
        long result = dbHelper.insertUser(db, name, email, password, "customer"); // 默认角色为customer
        if (result != -1) {
            Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();

            // Assuming dbHelper.insertUser returns the user id
            String userId = String.valueOf(result);
            String userEmail = email;

            // Set user information in RoleManager
            RoleManager.setUserId(userId);
            RoleManager.setUserEmail(userEmail);

            // Navigate to the next screen
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to validate user input
    private boolean isValidInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            nameEditText.setError("姓名是必填项");
            nameEditText.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            emailEditText.setError("电子邮件是必填项");
            emailEditText.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("密码是必填项");
            passwordEditText.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("确认密码是必填项");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("密码不匹配");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("密码必须至少6个字符");
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
