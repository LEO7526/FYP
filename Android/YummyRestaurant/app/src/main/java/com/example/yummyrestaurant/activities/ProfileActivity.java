package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.utils.RoleManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNameText, userEmailText;
    private ImageView profileImage;
    private Button editButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        profileImage = findViewById(R.id.profileImage);
        editButton = findViewById(R.id.editButton);

        // Load user data
        String name = RoleManager.getUserName();
        String email = RoleManager.getUserEmail();
        String imageUrl = RoleManager.getUserImageUrl(); // Optional: profile image URL

        userNameText.setText(name);
        userEmailText.setText(email);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(profileImage);

        editButton.setOnClickListener(v -> {
                startActivity(new Intent(this, EditProfileActivity.class));
        });
    }
}