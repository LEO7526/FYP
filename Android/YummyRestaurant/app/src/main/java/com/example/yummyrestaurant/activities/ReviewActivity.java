package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReviewActivity extends AppCompatActivity {

    // UI Components
    private RatingBar ratingBar;
    private TextView ratingText;

    // Firebase References
    private FirebaseDatabase database;
    private DatabaseReference reviewsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        reviewsRef = database.getReference("reviews");

        // Initialize UI components
        ratingBar = findViewById(R.id.ratingBar);
        ratingText = findViewById(R.id.ratingText);

        // Set RatingBar change listener
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            ratingText.setText("You rated: " + rating);

            // Save the rating to Firebase
            saveRatingToFirebase(rating);
        });
    }

    // Method to save rating to Firebase
    private void saveRatingToFirebase(double rating) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String userEmail = currentUser.getEmail();

            // Create a review object
            Review review = new Review(userId, userEmail, rating);

            // Save the review to Firebase
            reviewsRef.push().setValue(review).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Rating saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save rating", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}

// Review POJO class for Firebase
class Review {
    String userId;
    String userEmail;
    double rating;

    Review() {} // Default constructor for Firebase

    Review(String userId, String userEmail, double rating) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.rating = rating;
    }
}
