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
import androidx.core.content.ContextCompat;

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

        Log.d(TAG, "onCreate: Initializing UI components");

        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        profileImage = findViewById(R.id.profileImage);
        editButton = findViewById(R.id.editButton);

        String name = RoleManager.getUserName();
        String email = RoleManager.getUserEmail();
        String imagePath = getIntent().getStringExtra("updatedImageUrl");
        String fallbackImageUrl = RoleManager.getUserImageUrl();

        Log.d(TAG, "onCreate: Retrieved name = " + name + ", email = " + email);
        Log.d(TAG, "onCreate: Intent imagePath = " + imagePath);
        Log.d(TAG, "onCreate: Fallback imagePath = " + fallbackImageUrl);

        String finalImagePath = (imagePath != null && !imagePath.isEmpty())
                ? imagePath
                : (fallbackImageUrl != null ? fallbackImageUrl : "");

        Log.d(TAG, "onCreate: Final image path = " + finalImagePath);
        loadProfileImage(finalImagePath);

        userNameText.setText(name != null ? name : "Unknown");
        userEmailText.setText(email != null ? email : "No email");

        editButton.setOnClickListener(v -> {
            Log.d(TAG, "Edit button clicked");
            startActivity(new Intent(this, EditProfileActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Refreshing profile image");

        String imagePath = getIntent().getStringExtra("updatedImageUrl");
        if (imagePath == null || imagePath.isEmpty()) {
            String fallbackImage = RoleManager.getUserImageUrl();
            imagePath = (fallbackImage != null) ? fallbackImage : "";
            Log.d(TAG, "onResume: Using fallback image path = " + imagePath);
        } else {
            Log.d(TAG, "onResume: Using updated image path = " + imagePath);
        }

        loadProfileImage(imagePath);
    }

    private void loadProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            String fullImageUrl = RetrofitClient.getBASE_Simulator_URL() + imagePath;
            Log.d(TAG, "loadProfileImage: Loading image from URL = " + fullImageUrl);

            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.error_layer)
                    .into(profileImage);

            profileImage.post(() -> {
                Drawable currentDrawable = profileImage.getDrawable();
                Drawable errorDrawable = ContextCompat.getDrawable(this, R.drawable.error_layer);

                if (currentDrawable != null && currentDrawable.getConstantState() != null &&
                        errorDrawable != null && errorDrawable.getConstantState() != null &&
                        currentDrawable.getConstantState().equals(errorDrawable.getConstantState())) {

                    Log.w(TAG, "loadProfileImage: Error drawable detected, applying pulse animation");
                    Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
                    profileImage.startAnimation(pulse);
                } else {
                    Log.d(TAG, "loadProfileImage: Image loaded successfully or not matching error drawable");
                }
            });
        } else {
            Log.w(TAG, "loadProfileImage: No image path provided, using default avatar");
            profileImage.setImageResource(R.drawable.default_avatar);
        }
    }
}