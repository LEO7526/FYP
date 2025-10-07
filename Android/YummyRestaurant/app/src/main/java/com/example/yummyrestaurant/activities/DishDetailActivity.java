package com.example.yummyrestaurant.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.MenuItem;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DishDetailActivity extends AppCompatActivity {

    private MenuItem item;                // current menu item shown

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtain menu item
        item = (MenuItem) getIntent().getSerializableExtra("menuItem");

        TextView name = findViewById(R.id.dishNameDetail);
        TextView description = findViewById(R.id.dishDescriptionDetail);
        TextView price = findViewById(R.id.dishPriceDetail);
        ImageView image = findViewById(R.id.dishImageDetail);
        LinearLayout spiceBar = findViewById(R.id.spiceBarDetail);

        if (item != null) {
            name.setText(item.getName() != null ? item.getName() : "Unknown Dish");
            description.setText(item.getDescription() != null ? item.getDescription() : "No description available.");
            price.setText(String.format(Locale.getDefault(), "¥ %.2f", item.getPrice()));

            LinearLayout tagsContainer = findViewById(R.id.tagsContainer);
            List<String> itemTags = item.getTags();
            if (itemTags != null && !itemTags.isEmpty()) {
                tagsContainer.removeAllViews();
                for (String rawTag : itemTags) {
                    String tag = rawTag == null ? "" : rawTag.trim();
                    if (tag.isEmpty()) continue;

                    TextView tagView = new TextView(this);
                    tagView.setText("#" + tag);
                    tagView.setTextSize(14);
                    tagView.setTextColor(Color.parseColor("#333333"));
                    tagView.setBackgroundResource(R.drawable.tag_background);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    int marginPx = (int) (8 * getResources().getDisplayMetrics().density);
                    params.setMargins(marginPx, marginPx, marginPx, marginPx);
                    tagView.setLayoutParams(params);

                    tagsContainer.addView(tagView);
                }
            } else {
                tagsContainer.removeAllViews();
            }

            String imageUrl = item.getImage_url();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_image)
                        .into(image);
            } else {
                image.setImageResource(R.drawable.placeholder);
            }

            // Spice level bar
            spiceBar.removeAllViews();
            int spiceCount = 0;
            try {
                spiceCount = Math.max(0, Math.min(4, item.getSpice_level()));
            } catch (Exception e) {
                spiceCount = 0;
            }

            List<String> spiceColors = Arrays.asList("#FFECB3", "#FFC107", "#FF9800", "#F44336");
            int segmentWidthPx = (int) (24 * getResources().getDisplayMetrics().density);
            int segmentHeightPx = (int) (8 * getResources().getDisplayMetrics().density);
            int gapPx = (int) (4 * getResources().getDisplayMetrics().density);

            for (int i = 0; i < spiceCount; i++) {
                TextView segment = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(segmentWidthPx, segmentHeightPx);
                if (i > 0) params.setMarginStart(gapPx);
                segment.setLayoutParams(params);
                segment.setBackgroundColor(Color.parseColor(spiceColors.get(Math.min(i, spiceColors.size() - 1))));
                spiceBar.addView(segment);
            }
            if (spiceCount == 0) {
                TextView defaultSegment = new TextView(this);
                defaultSegment.setLayoutParams(new LinearLayout.LayoutParams(segmentWidthPx, segmentHeightPx));
                defaultSegment.setBackgroundColor(Color.parseColor("#BDBDBD"));
                spiceBar.addView(defaultSegment);
            }

        } else {
            name.setText("Dish not found");
            description.setText("Unable to load dish details.");
            price.setText("$ --");
            image.setImageResource(R.drawable.error_image);
        }
    }
}