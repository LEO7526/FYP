package com.example.yummyrestaurant.activities;


import android.content.Intent;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.yummyrestaurant.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCustomerActivity extends AppCompatActivity {

    protected List<ImageView> functionIcons = new ArrayList<>();
    protected Map<ImageView, String> iconBaseNames = new HashMap<>();

    private boolean login ;

    @Override
    protected void onResume() {
        super.onResume();
        int selectedIconId = getIntent().getIntExtra("selectedIcon", 0); // no default highlight
        if (selectedIconId != 0) {
            ImageView selectedIcon = findViewById(selectedIconId);
            if (selectedIcon != null) {
                highlightIcon(selectedIcon);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected void setupBottomFunctionBar() {

        login = BrowseMenuActivity.getLogin();

        ImageView orderBellIcon = findViewById(R.id.orderBellIcon);
        ImageView couponIcon = findViewById(R.id.couponIcon);
        ImageView membershipIcon = findViewById(R.id.membershipIcon);
        ImageView orderRecordIcon = findViewById(R.id.orderRecordIcon);
        ImageView profileIcon = findViewById(R.id.profileIcon);

        functionIcons.clear();
        functionIcons.add(orderBellIcon);
        functionIcons.add(couponIcon);
        functionIcons.add(membershipIcon);
        functionIcons.add(orderRecordIcon);
        functionIcons.add(profileIcon);

        iconBaseNames.put(orderBellIcon, "customer_main_page_function_item_background");
        iconBaseNames.put(couponIcon, "customer_main_page_function_item_background");
        iconBaseNames.put(membershipIcon, "customer_main_page_function_item_background_unique");
        iconBaseNames.put(orderRecordIcon, "customer_main_page_function_item_background");
        iconBaseNames.put(profileIcon, "customer_main_page_function_item_background");

        // Set click listeners to navigate
        orderBellIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, BrowseMenuActivity.class);
            intent.putExtra("selectedIcon", R.id.orderBellIcon);
            startActivity(intent);
        });

        couponIcon.setOnClickListener(v -> {
            if (login) {
                Intent intent = new Intent(this, CouponActivity.class);
                intent.putExtra("selectedIcon", R.id.couponIcon); // tell Home which icon to highlight
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        membershipIcon.setOnClickListener(v -> {
            if (login) {
                Intent intent = new Intent(this, MembershipActivity.class);
                intent.putExtra("selectedIcon", R.id.membershipIcon); // tell Home which icon to highlight
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }

        });

        orderRecordIcon.setOnClickListener(v -> {

            if (login) {
                Intent intent = new Intent(this, OrderHistoryActivity.class);
                intent.putExtra("selectedIcon", R.id.orderRecordIcon); // tell Home which icon to highlight
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        profileIcon.setOnClickListener(v -> {

            if (login) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("selectedIcon", R.id.profileIcon); // tell Home which icon to highlight
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }

    protected void highlightIcon(ImageView selectedIcon) {
        for (ImageView icon : functionIcons) {
            String baseName = iconBaseNames.get(icon);
            if (baseName == null) continue;

            int drawableId = (icon == selectedIcon)
                    ? getResources().getIdentifier(baseName + "_current", "drawable", getPackageName())
                    : getResources().getIdentifier(baseName, "drawable", getPackageName());

            if (drawableId != 0) {
                icon.setBackgroundResource(drawableId);
            }
        }
    }
}