package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.database.DatabaseHelper;
import com.example.yummyrestaurant.utils.RoleManager;

public class ReviewActivity extends AppCompatActivity {

    // UI Components
    private RatingBar ratingBar;
    private TextView ratingText;

    // Database Helper
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        // Initialize UI components
        ratingBar = findViewById(R.id.ratingBar);
        ratingText = findViewById(R.id.ratingText);

        // Set RatingBar change listener
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            ratingText.setText("您评分为: " + rating);

            // Save the rating to SQLite
            saveRatingToSQLite(rating);
        });
    }

    // Method to save rating to SQLite
    private void saveRatingToSQLite(double rating) {
        String userId = RoleManager.getUserId(); // 获取当前用户的ID
        String userEmail = RoleManager.getUserEmail(); // 获取当前用户的电子邮件

        if (userId != null && userEmail != null) {
            // Insert the review into the SQLite database
            long result = dbHelper.insertReview(db, userId, userEmail, rating);
            if (result != -1) {
                Toast.makeText(this, "评分保存成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "保存评分失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
