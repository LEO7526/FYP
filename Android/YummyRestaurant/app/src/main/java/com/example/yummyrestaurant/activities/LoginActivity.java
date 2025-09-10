package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.database.DatabaseHelper;
import com.example.yummyrestaurant.utils.RoleManager;
import com.example.yummyrestaurant.utils.RoleManager.RoleCallback;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        // Initialize UI components
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginBtn);

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
            // Validate user credentials
            Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_EMAIL + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?", new String[]{email, password});
            if (cursor.getCount() > 0) {
                Toast.makeText(this, "Login sucessful", Toast.LENGTH_SHORT).show();
                cursor.moveToFirst();
                int userIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int userEmailIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL);
                int userRoleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE);

                if (userIdIndex == -1 || userEmailIndex == -1 || userRoleIndex == -1) {
                    Toast.makeText(this, "No such database", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    return;
                }

                String userId = cursor.getString(userIdIndex);
                String userEmail = cursor.getString(userEmailIndex);
                String role = cursor.getString(userRoleIndex);

                // Set user information in RoleManager
                RoleManager.setUserId(userId);
                RoleManager.setUserEmail(userEmail);
                RoleManager.setUserRole(role);

                cursor.close();

                // Navigate to the next screen
                if ("staff".equals(role)) {
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                } else {
                    startActivity(new Intent(LoginActivity.this, ProductListActivity.class));
                }
                finish(); // Close LoginActivity after redirect
            } else {
                Toast.makeText(this, "电子邮件或密码错误", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        } else {
            Toast.makeText(this, "请输入电子邮件和密码", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
