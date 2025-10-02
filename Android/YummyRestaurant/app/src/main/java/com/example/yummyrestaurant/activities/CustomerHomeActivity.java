package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.yummyrestaurant.R;

public class CustomerHomeActivity extends BaseCustomerActivity {

    private ImageView imageView;
    private LinearLayout dotsContainer;
    private ImageButton btnPrev, btnNext;

    private int[] images = {
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3
    };

    private int index = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean paused = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!paused) {
                showImage(index);
                index = (index + 1) % images.length;
            }
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        setupBottomFunctionBar();

        imageView = findViewById(R.id.myImageView);
        dotsContainer = findViewById(R.id.dotsContainer);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        setupDots();
        handler.post(runnable);

        // Press to pause, release to advance immediately
        imageView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    paused = true;
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    paused = false;
                    index = (index + 1) % images.length;
                    showImage(index);
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 3000);
                    return true;
            }
            return false;
        });

        // Manual navigation
        btnPrev.setOnClickListener(v -> {
            // Stop current loop
            handler.removeCallbacks(runnable);

            // Move to previous image
            index = (index - 1 + images.length) % images.length;
            showImage(index);

            // Resume auto-slideshow
            paused = false;
            handler.postDelayed(runnable, 3000);
        });

        btnNext.setOnClickListener(v -> {
            // Stop current loop
            handler.removeCallbacks(runnable);

            // Move to next image
            index = (index + 1) % images.length;
            showImage(index);

            // Resume auto-slideshow
            paused = false;
            handler.postDelayed(runnable, 3000);
        });
    }

    private void setupDots() {
        dotsContainer.removeAllViews();
        for (int i = 0; i < images.length; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_unselected);
            dotsContainer.addView(dot);
        }
        updateDots(0);
    }

    private void showImage(int position) {
        imageView.setImageResource(images[position]);
        updateDots(position);
    }

    private void updateDots(int selectedIndex) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            dot.setBackgroundResource(i == selectedIndex
                    ? R.drawable.dot_selected
                    : R.drawable.dot_unselected);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}