package com.example.yummyrestaurant.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Patterns;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.api.LoginCustomerApi;
import com.example.yummyrestaurant.api.UploadApi;
import com.example.yummyrestaurant.models.UploadResponse;
import com.example.yummyrestaurant.utils.RoleManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private EditText nameInput, emailInput;
    private ImageView profilePreview;
    private Button selectImageButton, saveButton;
    private Uri selectedImageUri;
    private LoginCustomerApi apiService;

    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Log.d(TAG, "Image selected: " + selectedImageUri);
                    Glide.with(this).load(selectedImageUri).into(profilePreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        profilePreview = findViewById(R.id.profilePreview);
        selectImageButton = findViewById(R.id.selectImageButton);
        saveButton = findViewById(R.id.saveButton);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);

        apiService = RetrofitClient.getClient().create(LoginCustomerApi.class);

        // Request permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            }
        }

        // Load existing data
        String currentName = RoleManager.getUserName();
        String currentEmail = RoleManager.getUserEmail();
        String imagePath = RoleManager.getUserImageUrl();

        Log.d(TAG, "Loaded user data: name=" + currentName + ", email=" + currentEmail + ", imagePath=" + imagePath);

        nameInput.setText(currentName != null ? currentName : "");
        emailInput.setText(currentEmail != null ? currentEmail : "");

        if (imagePath != null && !imagePath.isEmpty()) {
            String fullImageUrl = RetrofitClient.getBASE_Simulator_URL() + imagePath;
            Log.d(TAG, "Loading profile image from: " + fullImageUrl);

            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.error_layer) // Show layered error drawable
                    .into(profilePreview);

            // Apply pulse animation if image fails to load
            profilePreview.post(() -> {
                Drawable currentDrawable = profilePreview.getDrawable();
                if (currentDrawable != null && currentDrawable.getConstantState() == getResources().getDrawable(R.drawable.error_layer).getConstantState()) {
                    Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
                    profilePreview.startAnimation(pulse);
                }
            });
        }

        selectImageButton.setOnClickListener(v -> {
            Log.d(TAG, "Opening image picker...");
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        saveButton.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String newEmail = emailInput.getText().toString().trim();

            Log.d(TAG, "Save button clicked. New name: " + newName + ", New email: " + newEmail);

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Name and email cannot be empty", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Validation failed: empty fields");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Validation failed: invalid email");
                return;
            }

            RoleManager.setUserName(newName);
            RoleManager.setUserEmail(newEmail);

            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    byte[] imageBytes = readBytes(inputStream);

                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
                    String fileName = getFileNameFromUri(selectedImageUri); // ✅ actual filename

                    MultipartBody.Part body = MultipartBody.Part.createFormData(
                            "image",
                            fileName,
                            requestFile
                    );
                    RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), newEmail);

                    UploadApi uploadApi = RetrofitClient.getClient().create(UploadApi.class);
                    Call<UploadResponse> uploadCall = uploadApi.uploadImage(body, emailBody);

                    Log.d(TAG, "Uploading image to server...");
                    progressDialog.show();

                    uploadCall.enqueue(new Callback<UploadResponse>() {
                        @Override
                        public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                            progressDialog.dismiss();
                            if (response.isSuccessful() && response.body() != null) {
                                UploadResponse uploadResponse = response.body();
                                if ("success".equals(uploadResponse.getStatus())) {
                                    RoleManager.setUserImageUrl(uploadResponse.getPath());

                                    String fullImageUrl = RetrofitClient.getBASE_Simulator_URL() + RoleManager.getUserImageUrl();
                                    Glide.with(EditProfileActivity.this)
                                            .load(fullImageUrl)
                                            .placeholder(R.drawable.default_avatar)
                                            .error(R.drawable.error_image)
                                            .into(profilePreview);

                                    Toast.makeText(EditProfileActivity.this, "✅ Image uploaded!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                                    intent.putExtra("updatedImageUrl", RoleManager.getUserImageUrl());
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(EditProfileActivity.this, "❌ Upload failed: " + uploadResponse.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(EditProfileActivity.this, "❌ Server error: " + response.code(), Toast.LENGTH_LONG).show();
                            }

                            new android.os.Handler().postDelayed(() -> {
                                startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                                finish();
                            }, 1500);
                        }

                        @Override
                        public void onFailure(Call<UploadResponse> call, Throwable t) {
                            progressDialog.dismiss();
                            Log.e(TAG, "Upload error", t);
                            Toast.makeText(EditProfileActivity.this, "⚠️ Upload error: " + t.getMessage(), Toast.LENGTH_LONG).show();

                            new AlertDialog.Builder(EditProfileActivity.this)
                                    .setTitle("Upload Failed")
                                    .setMessage("Would you like to retry?")
                                    .setPositiveButton("Retry", (dialog, which) -> saveButton.performClick())
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "File access error", e);
                    Toast.makeText(this, "⚠️ Unable to access image file", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d(TAG, "No image selected. Proceeding with profile update...");
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            }
        });
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == 100 || requestCode == 101) && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied. Cannot access image.", Toast.LENGTH_LONG).show();
        }
    }

    public String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment(); // fallback
        }

        return result;
    }
}