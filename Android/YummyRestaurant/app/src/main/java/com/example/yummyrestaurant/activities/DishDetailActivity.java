package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class DishDetailActivity extends BaseCustomerActivity {

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
            price.setText(String.format(Locale.getDefault(), "$ %.2f", item.getPrice()));

            // Tags
            LinearLayout tagsContainer = findViewById(R.id.tagsContainer);
            if (item.getTags() != null && !item.getTags().isEmpty()) {
                tagsContainer.removeAllViews();
                for (String rawTag : item.getTags()) {
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

            // Image
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
            int spiceLevel = item.getSpice_level();
            int spiceCount;
            switch (spiceLevel) {
                case 1: spiceCount = 1; break;
                case 2: spiceCount = 2; break;
                case 3: spiceCount = 3; break;
                case 4: spiceCount = 4; break;
                default: spiceCount = 0; break;
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

            // Add to Cart button
            addToCartBtn.setOnClickListener(v -> {
                CartItem cartItem = new CartItem(item, selectedCustomization);

                Integer qtyFromCart = CartManager.getItemQuantity(cartItem);
                int currentQty = (qtyFromCart != null) ? qtyFromCart : 0;

                String pendingSpice = (selectedCustomization != null) ? selectedCustomization.getSpiceLevel() : null;
                String pendingNotes = (selectedCustomization != null) ? selectedCustomization.getExtraNotes() : null;

                if (BrowseMenuActivity.isLogin()) {
                    // Update cart immediately
                    CartManager.updateQuantity(cartItem, currentQty + quantity[0]);

                    String customizationText = (pendingSpice != null) ? " (" + pendingSpice + ")" : "";
                    Toast.makeText(
                            this,
                            quantity[0] + " × " + item.getName() + customizationText,
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    // Defer cart action until after login
                    navigateProtected(
                            R.id.addToCartBtn,
                            CartActivity.class,   // go to cart after login
                            item,
                            currentQty + quantity[0],
                            pendingSpice,
                            pendingNotes
                    );
                }
            });

        } else {
            name.setText("Dish not found");
            description.setText("Unable to load dish details.");
            price.setText("$ --");
            image.setImageResource(R.drawable.error_image);
        }

        // Customize button
        Button customizeBtn = findViewById(R.id.customizeBtn);
        customizeBtn.setOnClickListener(v -> {
            if (BrowseMenuActivity.isLogin()) {
                Intent intent = new Intent(this, CustomizeDishActivity.class);
                intent.putExtra(CustomizeDishActivity.EXTRA_MENU_ITEM, item);
                intent.putExtra(CustomizeDishActivity.EXTRA_QUANTITY, quantity[0]);
                startActivity(intent);
            } else {
                navigateProtected(
                        R.id.customizeBtn,
                        CustomizeDishActivity.class,
                        null, 0, null, null   // ✅ just gate access, don’t add to cart
                );
            }
        });
    }
}