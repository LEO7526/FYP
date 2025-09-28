package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Customization;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DishDetailActivity extends AppCompatActivity {

    private static final int REQUEST_CUSTOMIZE = 1001;
    private Customization selectedCustomization = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MenuItem item = (MenuItem) getIntent().getSerializableExtra("menuItem");

        TextView name = findViewById(R.id.dishNameDetail);
        TextView description = findViewById(R.id.dishDescriptionDetail);
        TextView price = findViewById(R.id.dishPriceDetail);
        ImageView image = findViewById(R.id.dishImageDetail);
        LinearLayout spiceBar = findViewById(R.id.spiceBarDetail);
        Button addToCartBtn = findViewById(R.id.addToCartBtn);

        // Quantity controls
        Button decreaseBtn = findViewById(R.id.decreaseQtyBtn);
        Button increaseBtn = findViewById(R.id.increaseQtyBtn);
        TextView quantityText = findViewById(R.id.quantityText);

        final int[] quantity = {1};
        decreaseBtn.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                quantityText.setText(String.valueOf(quantity[0]));
            }
        });
        increaseBtn.setOnClickListener(v -> {
            quantity[0]++;
            quantityText.setText(String.valueOf(quantity[0]));
        });

        if (item != null) {
            name.setText(item.getName() != null ? item.getName() : "Unknown Dish");
            description.setText(item.getDescription() != null ? item.getDescription() : "No description available.");
            price.setText(String.format(Locale.getDefault(), "¥ %.2f", item.getPrice()));

            LinearLayout tagsContainer = findViewById(R.id.tagsContainer);
            if (item.getTags() != null && !item.getTags().isEmpty()) {
                tagsContainer.removeAllViews();
                String[] tags = item.getTags().split(",");
                for (String rawTag : tags) {
                    String tag = rawTag.trim();
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
                    params.setMargins(8, 8, 8, 8);
                    tagView.setLayoutParams(params);

                    tagsContainer.addView(tagView);
                }
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
            String spice = item.getSpice_level() != null ? item.getSpice_level().toLowerCase() : "";
            int spiceCount;
            switch (spice) {
                case "mild":
                    spiceCount = 1;
                    break;
                case "medium":
                    spiceCount = 2;
                    break;
                case "hot":
                    spiceCount = 3;
                    break;
                case "numbing":
                    spiceCount = 4;
                    break;
                default:
                    spiceCount = 0;
                    break;
            }
            List<String> spiceColors = Arrays.asList("#FFECB3", "#FFC107", "#FF9800", "#F44336");
            for (int i = 0; i < spiceCount; i++) {
                TextView segment = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 8);
                if (i > 0) params.setMarginStart(4);
                segment.setLayoutParams(params);
                segment.setBackgroundColor(Color.parseColor(spiceColors.get(i)));
                spiceBar.addView(segment);
            }
            if (spiceCount == 0) {
                TextView defaultSegment = new TextView(this);
                defaultSegment.setLayoutParams(new LinearLayout.LayoutParams(24, 8));
                defaultSegment.setBackgroundColor(Color.parseColor("#BDBDBD"));
                spiceBar.addView(defaultSegment);
            }

            /// Add to Cart button logic
            addToCartBtn.setOnClickListener(v -> {
                if (CustomerHomeActivity.isLogin()) {
                    // User is logged in → add directly
                    CartItem cartItem = new CartItem(item, selectedCustomization);
                    int currentQty = CartManager.getItemQuantity(cartItem);
                    CartManager.updateQuantity(cartItem, currentQty + quantity[0]);

                    Toast.makeText(
                            this,
                            quantity[0] + " × " + item.getName() +
                                    (selectedCustomization != null && selectedCustomization.getSpiceLevel() != null
                                            ? " (" + selectedCustomization.getSpiceLevel() + ")"
                                            : ""),
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    // User not logged in → redirect to LoginActivity with pending extras
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("pendingMenuItem", item); // MenuItem must implement Serializable/Parcelable
                    intent.putExtra("pendingQuantity", quantity[0]);

                    if (selectedCustomization != null) {
                        intent.putExtra("pendingSpice", selectedCustomization.getSpiceLevel());
                        intent.putExtra("pendingNotes", selectedCustomization.getExtraNotes());
                    }

                    startActivity(intent);
                }
            });

        } else {
            name.setText("Dish not found");
            description.setText("Unable to load dish details.");
            price.setText("¥ --");
            image.setImageResource(R.drawable.error_image);
        }

        // Launch customization
        Button customizeBtn = findViewById(R.id.customizeBtn);
        customizeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomizeDishActivity.class);
            startActivityForResult(intent, REQUEST_CUSTOMIZE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CUSTOMIZE && resultCode == RESULT_OK && data != null) {
            String spiceLevel = data.getStringExtra(CustomizeDishActivity.EXTRA_SPICE_LEVEL);
            String extraNotes = data.getStringExtra(CustomizeDishActivity.EXTRA_NOTES);

            selectedCustomization = new Customization(spiceLevel, extraNotes);

            Toast.makeText(
                    this,
                    "Customization saved: " + spiceLevel +
                            (extraNotes != null && !extraNotes.isEmpty() ? " (" + extraNotes + ")" : ""),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}