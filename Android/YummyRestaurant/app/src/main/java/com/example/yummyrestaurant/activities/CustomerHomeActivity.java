package com.example.yummyrestaurant.activities;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;

public class CustomerHomeActivity extends BaseCustomerActivity {

    private ImageView imageView;
    private int[] images = {
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3
    };

    private int index = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Change image
            imageView.setImageResource(images[index]);

            // Move to next index
            index = (index + 1) % images.length;

            // Repeat every 3 seconds
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        setupBottomFunctionBar(); // reuse the same bar + highlight logic

        imageView = findViewById(R.id.myImageView);

        // Start slideshow
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop handler to prevent memory leaks
        handler.removeCallbacks(runnable);
    }

}