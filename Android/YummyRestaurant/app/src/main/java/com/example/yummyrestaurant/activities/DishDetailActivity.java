package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.LoginBottomSheetFragment;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Customization;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.BadgeManager;
import com.example.yummyrestaurant.utils.CartManager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DishDetailActivity extends AppCompatActivity {

    private Customization selectedCustomization = null;
    private ActivityResultLauncher<Intent> customizeLauncher;
    private MenuItem item;                // current menu item shown
    private int currentQuantity = 1;      // quantity chosen on UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Restore saved customization/quantity after configuration change
        if (savedInstanceState != null) {
            String savedSpice = savedInstanceState.getString("cust_spice");
            String savedNotes = savedInstanceState.getString("cust_notes");
            if (savedSpice != null || savedNotes != null) {
                selectedCustomization = new Customization(savedSpice, savedNotes);
                android.util.Log.d("CartDebug", "onCreate restored selectedCustomization: " + selectedCustomization);
            }
            currentQuantity = savedInstanceState.getInt("cust_qty", 1);
        }

        // Activity Result launcher for customization (will auto-add to cart on save)
        customizeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    android.util.Log.d("CartDebug", "ActivityResult callback: " + result);
                    if (result != null && result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String spice = data.getStringExtra(CustomizeDishActivity.EXTRA_SPICE_LEVEL);
                        String notes = data.getStringExtra(CustomizeDishActivity.EXTRA_NOTES);
                        android.util.Log.d("CartDebug", "Customization returned: spice=" + spice + " notes=" + notes);

                        // set selected customization
                        selectedCustomization = new Customization(spice, notes);
                        android.util.Log.d("CartDebug", "selectedCustomization set: " + selectedCustomization);

                        // Immediately add customized item to cart using currentQuantity
                        if (item != null) {
                            CartItem cartItem = new CartItem(item, selectedCustomization);
                            android.util.Log.d("CartDebug", "Auto-adding customized item to cart: menuId="
                                    + stableMenuItemIdForLogging(item) + " qty=" + currentQuantity);
                            CartManager.addItem(cartItem, currentQuantity);
                            CartActivity.refreshCartUI();

                            BadgeManager.updateCartBadge(CartManager.getTotalItems());




                            // Log snapshot after add
                            for (Map.Entry<CartItem, Integer> e : CartManager.getCartItems().entrySet()) {
                                android.util.Log.d("CartDebug", "post-auto-add snapshot keyHash=" + e.getKey().hashCode()
                                        + " stableId=" + stableMenuItemIdForLogging(e.getKey().getMenuItem())
                                        + " customization=" + e.getKey().getCustomization()
                                        + " qty=" + e.getValue());
                            }

                            Toast.makeText(this, currentQuantity + " × " +
                                    (item.getName() == null ? "" : item.getName()) +
                                    (selectedCustomization != null && selectedCustomization.getSpiceLevel() != null
                                            ? " (" + selectedCustomization.getSpiceLevel() + ")"
                                            : "") + " added to cart", Toast.LENGTH_SHORT).show();
                        } else {
                            android.util.Log.d("CartDebug", "Auto-add skipped: item == null");
                            Toast.makeText(this, "Customization saved", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        android.util.Log.d("CartDebug", "Customization canceled or no data returned");
                    }
                }
        );

        // Obtain menu item
        item = (MenuItem) getIntent().getSerializableExtra("menuItem");

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

        currentQuantity = 1;
        quantityText.setText(String.valueOf(currentQuantity));
        decreaseBtn.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                quantityText.setText(String.valueOf(currentQuantity));
            }
        });
        increaseBtn.setOnClickListener(v -> {
            currentQuantity++;
            quantityText.setText(String.valueOf(currentQuantity));
        });

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

            // Add to Cart button logic (kept for plain-add / manual add if user wants)
            addToCartBtn.setOnClickListener(v -> {
                android.util.Log.d("CartDebug", "DishDetail add clicked: menuItemHash="
                        + (item != null ? item.hashCode() : "null")
                        + " selectedCustomization=" + selectedCustomization
                        + " qty=" + currentQuantity);

                CartItem cartItem = new CartItem(item, selectedCustomization);
                android.util.Log.d("CartDebug", "DishDetail cartItem built: keyHash=" + cartItem.hashCode()
                        + " menuStableId=" + stableMenuItemIdForLogging(item)
                        + " customization=" + cartItem.getCustomization());

                if (BrowseMenuActivity.isLogin()) {
                    CartManager.addItem(cartItem, currentQuantity);
                    CartActivity.refreshCartUI();


                    for (Map.Entry<CartItem, Integer> e : CartManager.getCartItems().entrySet()) {
                        android.util.Log.d("CartDebug", "post-add snapshot keyHash=" + e.getKey().hashCode()
                                + " stableId=" + stableMenuItemIdForLogging(e.getKey().getMenuItem())
                                + " customization=" + e.getKey().getCustomization()
                                + " qty=" + e.getValue());
                    }

                    Toast.makeText(
                            this,
                            currentQuantity + " × " + (item.getName() == null ? "" : item.getName()) +
                                    (selectedCustomization != null && selectedCustomization.getSpiceLevel() != null
                                            ? " (" + selectedCustomization.getSpiceLevel() + ")"
                                            : ""),
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();

                    // Prepare arguments for login bottom sheet
                    Bundle args = new Bundle();
                    args.putSerializable("pendingMenuItem", item);
                    args.putInt("pendingQuantity", currentQuantity);
                    if (selectedCustomization != null) {
                        args.putString("pendingSpice", selectedCustomization.getSpiceLevel());
                        args.putString("pendingNotes", selectedCustomization.getExtraNotes());
                    }

                    // Show inline login bottom sheet
                    LoginBottomSheetFragment sheet = new LoginBottomSheetFragment();
                    sheet.setArguments(args);
                    sheet.setLoginListener(success -> {
                        if (success) {
                            // After login, reuse existing cartItem
                            CartItem restoredItem = new CartItem(item, selectedCustomization);
                            CartManager.addItem(restoredItem, currentQuantity);
                            CartActivity.refreshCartUI();

                            Toast.makeText(
                                    this,
                                    currentQuantity + " × " + (item.getName() == null ? "" : item.getName()) +
                                            (selectedCustomization != null && selectedCustomization.getSpiceLevel() != null
                                                    ? " (" + selectedCustomization.getSpiceLevel() + ")"
                                                    : ""),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                    sheet.show(getSupportFragmentManager(), "login_sheet");
                }
            });

        } else {
            name.setText("Dish not found");
            description.setText("Unable to load dish details.");
            price.setText("$ --");
            image.setImageResource(R.drawable.error_image);
        }

        // Launch customization with Activity Result API
        Button customizeBtn = findViewById(R.id.customizeBtn);
        customizeBtn.setOnClickListener(v -> {
            // preserve currentQuantity when launching customize
            Intent intent = new Intent(this, CustomizeDishActivity.class);
            customizeLauncher.launch(intent);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedCustomization != null) {
            outState.putString("cust_spice", selectedCustomization.getSpiceLevel());
            outState.putString("cust_notes", selectedCustomization.getExtraNotes());
        }
        outState.putInt("cust_qty", currentQuantity);
    }

    private Object stableMenuItemIdForLogging(MenuItem menu) {
        if (menu == null) return null;
        try { return menu.getClass().getMethod("getId").invoke(menu); } catch (Exception ignored) {}
        try { return menu.getClass().getMethod("get_id").invoke(menu); } catch (Exception ignored) {}
        try { return menu.getClass().getMethod("getUuid").invoke(menu); } catch (Exception ignored) {}
        try {
            String name = (String) menu.getClass().getMethod("getName").invoke(menu);
            double price = (double) menu.getClass().getMethod("getPrice").invoke(menu);
            return (name == null ? "" : name) + "|" + price;
        } catch (Exception ignored) {}
        return menu.hashCode();
    }
}