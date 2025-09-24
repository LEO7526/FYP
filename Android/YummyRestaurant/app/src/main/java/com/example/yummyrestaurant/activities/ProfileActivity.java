package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.utils.RoleManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNameText, userEmailText;
    private ImageView profileImage;
    private Button editButton;

    private static final String TAG = "ProfileActivity";

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
        String fallbackImageUrl = RoleManager.getUserImageUrl();
        String imagePath = getIntent().getStringExtra("updatedImageUrl");

        // Use updated image if available, otherwise fallback
        String finalImagePath = (imagePath != null && !imagePath.isEmpty()) ? imagePath : fallbackImageUrl;

        if (finalImagePath != null && !finalImagePath.isEmpty()) {
            String fullImageUrl = RetrofitClient.getBASE_Simulator_URL() + finalImagePath;
            Log.d(TAG, "Displaying profile image: " + fullImageUrl);
        } else {
            Log.d(TAG, "No profile image to display.");
            profileImage.setImageResource(R.drawable.default_avatar);
        }

        userNameText.setText(name != null ? name : "Unknown");
        userEmailText.setText(email != null ? email : "No email");

        editButton.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String imagePath = getIntent().getStringExtra("updatedImageUrl");
        if (imagePath == null || imagePath.isEmpty()) {
            imagePath = RoleManager.getUserImageUrl();
        }

        if (imagePath != null && !imagePath.isEmpty()) {
            String fullImageUrl = RetrofitClient.getBASE_Simulator_URL() + imagePath;
            Log.d(TAG, "Refreshing profile image: " + fullImageUrl);

            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.error_layer) // Show error drawable if image fails
                    .into(profileImage);

            // Apply pulse animation if error drawable is shown
            profileImage.post(() -> {
                Drawable currentDrawable = profileImage.getDrawable();
                Drawable errorDrawable = getResources().getDrawable(R.drawable.error_layer);

                if (currentDrawable != null && currentDrawable.getConstantState() != null &&
                        currentDrawable.getConstantState().equals(errorDrawable.getConstantState())) {

                    Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
                    profileImage.startAnimation(pulse);
                }
            });
        } else {
            profileImage.setImageResource(R.drawable.default_avatar);
        }
    }
}