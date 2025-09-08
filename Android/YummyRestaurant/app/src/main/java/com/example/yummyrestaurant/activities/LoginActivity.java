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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginBtn);

        // 设置点击事件
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // 登录逻辑
            if (!email.isEmpty() && !password.isEmpty()) {
                // 触发登录逻辑
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });

        // 注册链接
        TextView registerLink = findViewById(R.id.registerLink);
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}


