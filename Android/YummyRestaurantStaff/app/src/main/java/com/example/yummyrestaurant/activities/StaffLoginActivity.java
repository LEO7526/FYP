package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StaffLoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_login);

        // 1. 妾㈡煡鏄惁宸茬稉鐧诲叆
        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            goToOrdersPage();
            return;
        }

        // 2. 缍佸畾 UI 鍏冧欢 (灏嶆噳 activity_staff_login.xml 瑁＄殑 ID)
        emailInput = findViewById(R.id.staffId);
        passwordInput = findViewById(R.id.staffPassword);
        loginBtn = findViewById(R.id.staffLoginBtn);

        // 3. 鎸夐垥榛炴搳浜嬩欢
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    performLogin(email, password);
                } else {
                    Toast.makeText(StaffLoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void performLogin(final String email, final String password) {
        loginBtn.setEnabled(false); // 闃叉閲嶈榛炴搳

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConstants.login(),
                response -> {
                    Log.d("LOGIN_RES", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");

                        if (status.equals("success")) {
                            int sid = jsonObject.getInt("sid");
                            String name = jsonObject.getString("name");
                            String role = jsonObject.getString("role");

                            session.createLoginSession(sid, name, role);
                            Toast.makeText(StaffLoginActivity.this, "Welcome " + name, Toast.LENGTH_SHORT).show();

                            goToOrdersPage();
                        } else {
                            loginBtn.setEnabled(true);
                            Toast.makeText(StaffLoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        loginBtn.setEnabled(true);
                        Toast.makeText(StaffLoginActivity.this, "Json Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    loginBtn.setEnabled(true);
                    Toast.makeText(StaffLoginActivity.this, "Network Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void goToOrdersPage() {
        Intent intent = new Intent(StaffLoginActivity.this, StaffOrdersActivity.class);
        startActivity(intent);
        finish();
    }
}

