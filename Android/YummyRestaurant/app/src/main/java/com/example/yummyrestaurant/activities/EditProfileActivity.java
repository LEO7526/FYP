package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.utils.RoleManager;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText nameInput, emailInput;
    private ImageView profilePreview;
    private Button selectImageButton, saveButton;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        profilePreview = findViewById(R.id.profilePreview);
        selectImageButton = findViewById(R.id.selectImageButton);
        saveButton = findViewById(R.id.saveButton);

        // Load existing data
        nameInput.setText(RoleManager.getUserName());
        emailInput.setText(RoleManager.getUserEmail());
        String imageUrl = RoleManager.getUserImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(Uri.parse(imageUrl)).into(profilePreview);
        }

        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        saveButton.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String newEmail = emailInput.getText().toString().trim();

            RoleManager.setUserName(newName);
            RoleManager.setUserEmail(newEmail);

            if (selectedImageUri != null) {
                RoleManager.setUserImageUrl(selectedImageUri.toString());
            }

            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(profilePreview);
        }
    }
}