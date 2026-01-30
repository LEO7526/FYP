package com.example.yummyrestaurant.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yummyrestaurant.R;

public class MembershipActivity extends BaseCustomerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        //parent's method
        setupBottomFunctionBar(); // reuse the same bar + highlight logic

    }
}
