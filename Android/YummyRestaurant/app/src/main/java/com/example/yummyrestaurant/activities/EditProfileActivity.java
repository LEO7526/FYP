package com.example.yummyrestaurant.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.CustomerUploadApi;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.api.LoginCustomerApi;
import com.example.yummyrestaurant.api.StaffUploadApi;
import com.example.yummyrestaurant.models.BirthdayResponse;
import com.example.yummyrestaurant.models.UploadResponse;
import com.example.yummyrestaurant.utils.RoleManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private EditText nameInput, emailInput,birthdayInput;
    private ImageView profilePreview;
    private Button selectImageButton, saveButton;
    private Uri selectedImageUri;
    private LoginCustomerApi apiService;

    private ProgressDialog progressDialog;

    private TextView birthdayStatusLabel;


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

        apiService = RetrofitClient.getClient(this).create(LoginCustomerApi.class);

        // Request storage access permission
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

        birthdayInput = findViewById(R.id.birthdayInput);
        birthdayStatusLabel = findViewById(R.id.birthdayStatusLabel);
        birthdayInput.setText(RoleManager.getUserBirthday() != null ? RoleManager.getUserBirthday() : "");

        birthdayInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    EditProfileActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format as MM-DD
                        String formatted = String.format(Locale.getDefault(), "%02d-%02d", selectedMonth + 1, selectedDay);
                        birthdayInput.setText(formatted);
                    },
                    calendar.get(Calendar.YEAR), month, day
            );

            // 👉 Insert the null‑safe year hiding code here
            int yearId = getResources().getIdentifier("year", "id", "android");
            View yearView = dialog.getDatePicker().findViewById(yearId);
            if (yearView != null) {
                yearView.setVisibility(View.GONE);
            }

            dialog.show();
        });

        if (RoleManager.getUserBirthday() != null && !RoleManager.getUserBirthday().isEmpty()) {
            birthdayInput.setEnabled(false);
            birthdayInput.setAlpha(0.6f); // visually dim it
        }

        if (imagePath != null && !imagePath.isEmpty()) {
            String fullImageUrl = imagePath; // Now imagePath is the full GitHub URL
            Log.d(TAG, "Loading profile image from: " + fullImageUrl);

            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.error_layer)
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
                    String fileName = getFileNameFromUri(selectedImageUri);

                    MultipartBody.Part body = MultipartBody.Part.createFormData(
                            "image",
                            fileName,
                            requestFile
                    );

                    Call<UploadResponse> uploadCall;

                    if (RoleManager.getUserRole().equals("customer")) {
                        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), newEmail);
                        CustomerUploadApi uploadApi = RetrofitClient.getClient(this).create(CustomerUploadApi.class);
                        uploadCall = uploadApi.uploadImage(body, emailBody); // API expects "cemail"
                    } else if (RoleManager.getUserRole().equals("staff")) {
                        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), newEmail);
                        StaffUploadApi uploadApi = RetrofitClient.getClient(this).create(StaffUploadApi.class);
                        uploadCall = uploadApi.uploadImage(body, emailBody); // API expects "semail"
                    } else {
                        Toast.makeText(this, "Unknown user role", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Upload aborted: unknown role");
                        return;
                    }

                    Log.d(TAG, "Uploading image to server...");
                    progressDialog.show();

                    uploadCall.enqueue(new Callback<UploadResponse>() {
                        @Override
                        public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                            progressDialog.dismiss();

                            Log.e(TAG, "Server response code: " + response.code());

                            if (response.isSuccessful() && response.body() != null) {
                                UploadResponse uploadResponse = response.body();
                                Log.d(TAG, "UploadResponse status: " + uploadResponse.getStatus());
                                Log.d(TAG, "UploadResponse path: " + uploadResponse.getPath());
                                Log.d(TAG, "UploadResponse message: " + uploadResponse.getMessage());

                                if ("success".equals(uploadResponse.getStatus())) {
                                    String fileName = extractFileNameFromPath(uploadResponse.getPath());
                                    String role = RoleManager.getUserRole();

                                    String githubBaseUrl = "https://raw.githubusercontent.com/LEO7526/FYP/main/Image/Profile_image/";
                                    String githubImageUrl;

                                    if ("staff".equals(role)) {
                                        githubImageUrl = githubBaseUrl + "Staff/" + fileName;
                                    } else {
                                        githubImageUrl = githubBaseUrl + "Customer/" + fileName;
                                    }

                                    RoleManager.setUserImageUrl(githubImageUrl);

                                    Glide.with(EditProfileActivity.this)
                                            .load(githubImageUrl)
                                            .placeholder(R.drawable.default_avatar)
                                            .error(R.drawable.error_image)
                                            .into(profilePreview);

                                    Toast.makeText(EditProfileActivity.this, "✅ Image uploaded!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                                    intent.putExtra("updatedImageUrl", githubImageUrl);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(EditProfileActivity.this, "❌ Upload failed: " + uploadResponse.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(EditProfileActivity.this, "❌ Server error: " + response.code(), Toast.LENGTH_LONG).show();

                                try {
                                    if (response.errorBody() != null) {
                                        String errorBody = response.errorBody().string();
                                        Log.e(TAG, "Error body content: " + errorBody);
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "Error reading errorBody", e);
                                }
                            }
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


            // Save birthday if applicable
            saveBirthdayIfNeeded();
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

    private String extractFileNameFromPath(String fullPath) {
        if (fullPath == null || fullPath.isEmpty()) {
            return "";
        }

        int lastSlashIndex = fullPath.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < fullPath.length() - 1) {
            return fullPath.substring(lastSlashIndex + 1);
        }

        return fullPath; // Return as-is if no slash found
    }

    private void saveBirthdayIfNeeded() {
        String birthday = birthdayInput.getText().toString().trim();
        String role = RoleManager.getUserRole();
        String email = RoleManager.getUserEmail();

        if ("customer".equals(role)
                && (RoleManager.getUserBirthday() == null || RoleManager.getUserBirthday().isEmpty())
                && !birthday.isEmpty()) {

            CustomerUploadApi birthdayApi = RetrofitClient.getClient(this).create(CustomerUploadApi.class);
            Call<BirthdayResponse> birthdayCall = birthdayApi.updateBirthday(email, birthday);

            birthdayCall.enqueue(new Callback<BirthdayResponse>() {
                @Override
                public void onResponse(Call<BirthdayResponse> call, Response<BirthdayResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        RoleManager.setUserBirthday(response.body().getCbirthday());
                        Log.d(TAG, "Birthday saved: " + response.body().getCbirthday());
                        Toast.makeText(EditProfileActivity.this, "Birthday saved!", Toast.LENGTH_SHORT).show();

                        // Disable the field immediately after saving
                        birthdayInput.setEnabled(false);
                        birthdayInput.setAlpha(0.6f);
                        birthdayInput.setText(RoleManager.getUserBirthday());

                        // Show status label
                        if (birthdayStatusLabel != null) {
                            birthdayStatusLabel.setText("Birthday saved");
                            birthdayStatusLabel.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.w(TAG, "Birthday save failed: " +
                                (response.body() != null ? response.body().getMessage() : "Unknown error"));
                    }
                }

                @Override
                public void onFailure(Call<BirthdayResponse> call, Throwable t) {
                    Log.e(TAG, "Birthday save error", t);
                }
            });
        }
    }

}