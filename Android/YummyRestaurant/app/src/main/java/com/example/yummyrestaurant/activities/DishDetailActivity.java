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
import com.example.yummyrestaurant.utils.MaterialAvailabilityChecker;

import org.json.JSONArray;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
            name.setText(item.getName() != null ? item.getName() : getString(R.string.unknown_dish));
            description.setText(item.getDescription() != null ? item.getDescription() : getString(R.string.no_description_available));
            price.setText(String.format(Locale.getDefault(), "$ %.2f", item.getPrice()));
            ViewCompat.setTransitionName(image, "dish_image_" + item.getId());

            // Tags
            LinearLayout tagsContainer = findViewById(R.id.tagsContainer);
            if (item.getTags() != null && !item.getTags().isEmpty()) {
                tagsContainer.removeAllViews();
                for (String rawTag : item.getTags()) {
                    String tag = rawTag.trim();
                    if (tag.isEmpty()) continue;

                    TextView tagView = new TextView(this);
                    tagView.setText("#" + getTranslatedTagName(tag));
                    tagView.setTextSize(12);
                    tagView.setTextColor(Color.parseColor("#333333"));
                    tagView.setPadding(12, 6, 12, 6);
                    
                    // 根據 tag 名稱設置顏色
                    String bgColor = getTagColor(tag);
                    tagView.setBackgroundColor(Color.parseColor(bgColor));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(6, 4, 6, 4);
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

            // Add to Cart button - only enable if order type is selected
            boolean orderTypeSelected = CartManager.isOrderTypeSelected();
            addToCartBtn.setEnabled(orderTypeSelected);
            if (!orderTypeSelected) {
                addToCartBtn.setAlpha(0.5f);
            }
            
            addToCartBtn.setOnClickListener(v -> {
                // Defensive check: should not happen but safety first
                if (!CartManager.isOrderTypeSelected()) {
                    Toast.makeText(this, getString(R.string.error_select_order_type_first), Toast.LENGTH_SHORT).show();
                    return;
                }
                
                CartItem cartItem = new CartItem(item, selectedCustomization);

                Integer qtyFromCart = CartManager.getItemQuantity(cartItem);
                int currentQty = (qtyFromCart != null) ? qtyFromCart : 0;

                String pendingSpice = (selectedCustomization != null) ? selectedCustomization.getSpiceLevel() : null;
                String pendingNotes = (selectedCustomization != null) ? selectedCustomization.getExtraNotes() : null;

                if (BrowseMenuActivity.isLogin()) {
                    // Check material availability before adding to cart
                    MaterialAvailabilityChecker.checkAdditionalQuantity(this, cartItem, quantity[0], 
                        new MaterialAvailabilityChecker.MaterialCheckCallback() {
                            @Override
                            public void onCheckComplete(boolean allAvailable, String message, JSONArray materialDetails) {
                                if (allAvailable) {
                                    // Materials are sufficient, add to cart
                                    CartManager.updateQuantity(cartItem, currentQty + quantity[0]);

                                    String customizationText = (pendingSpice != null) ? " (" + pendingSpice + ")" : "";
                                    Toast.makeText(
                                            DishDetailActivity.this,
                                            quantity[0] + " × " + item.getName() + customizationText,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                } else {
                                    // Insufficient materials, show detailed message
                                    String detailedMessage = MaterialAvailabilityChecker.formatInsufficientMaterialsMessage(materialDetails);
                                    Toast.makeText(
                                            DishDetailActivity.this,
                                            getString(R.string.cannot_add_to_cart_with_reason, detailedMessage),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }

                            @Override
                            public void onCheckError(String error) {
                                // On error, allow adding to cart but warn user
                                CartManager.updateQuantity(cartItem, currentQty + quantity[0]);
                                Toast.makeText(
                                        DishDetailActivity.this,
                                        getString(R.string.added_to_cart_unable_verify_ingredients, error),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
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
            name.setText(getString(R.string.dish_not_found));
            description.setText(getString(R.string.unable_load_dish_details));
            price.setText(getString(R.string.price_unavailable));
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

    /**
     * 根據 tag 名稱返回顏色代碼
     * 對應資料庫中定義的 tag 顏色
     */
    private String getTagColor(String tagName) {
        Map<String, String> tagColors = new java.util.HashMap<>();
        
        // 辣度/風味標籤
        tagColors.put("spicy", "#FFCDD2");      // 紅色淺色
        tagColors.put("numbing", "#E1BEE7");    // 紫色淺色
        tagColors.put("sour", "#C8E6C9");       // 綠色淺色
        tagColors.put("sweet", "#FFE0B2");      // 橙色淺色
        tagColors.put("salty", "#B2DFDB");      // 青色淺色
        
        // 食材標籤
        tagColors.put("chicken", "#FFECB3");    // 黃色淺色
        tagColors.put("beef", "#D1C4E9");       // 靛色淺色
        tagColors.put("pork", "#F8BBD0");       // 粉紅色淺色
        tagColors.put("seafood", "#B3E5FC");    // 淺藍色
        tagColors.put("vegetarian", "#C5E1A5"); // 淺綠色
        tagColors.put("fish", "#B3E5FC");       // 淺藍色
        tagColors.put("tofu", "#E0F2F1");       // 青綠色淺色
        tagColors.put("noodles", "#FFF8E1");    // 淺黃色

        // 飲品標籤
        tagColors.put("milk", "#FFE0B2");       // 橙色淺色
        tagColors.put("tea", "#BBDEFB");        // 藍色淺色
        tagColors.put("cold", "#E0F2F1");       // 青綠色淺色
        tagColors.put("hot", "#FFCCBC");        // 深橙色淺色
        tagColors.put("refreshing", "#B2DFDB"); // 青色淺色
        
        // 特性標籤
        tagColors.put("classic", "#DCEDC8");    // 淺綠色
        tagColors.put("traditional", "#F0F4C3"); // 檸檬色
        tagColors.put("glutinous", "#D1C4E9");  // 靛色淺色
        tagColors.put("lemon", "#FFF9C4");      // 淺黃色
        tagColors.put("soda", "#CFD8DC");       // 藍灰色淺色
        tagColors.put("grape", "#CE93D8");      // 紫色
        tagColors.put("streetfood", "#FFCCBC"); // 深橙色淺色
        tagColors.put("stirfry", "#C8E6C9");    // 綠色淺色
        
        // 返回 tag 對應的顏色，如果不存在則返回默認顏色
        return tagColors.getOrDefault(tagName.toLowerCase(), "#E8EAED");
    }

    /**
     * Translates a raw English tag name from the database into the current
     * locale using string resources. Falls back to the original name if no
     * translation key exists for it.
     */
    private String getTranslatedTagName(String tag) {
        if (tag == null) return "";
        switch (tag.trim().toLowerCase()) {
            case "vegetarian":  return getString(R.string.tag_vegetarian);
            case "spicy":       return getString(R.string.tag_spicy);
            case "numbing":     return getString(R.string.tag_numbing);
            case "sour":        return getString(R.string.tag_sour);
            case "sweet":       return getString(R.string.tag_sweet);
            case "salty":       return getString(R.string.tag_salty);
            case "chicken":     return getString(R.string.tag_chicken);
            case "beef":        return getString(R.string.tag_beef);
            case "pork":        return getString(R.string.tag_pork);
            case "seafood":     return getString(R.string.tag_seafood);
            case "fish":        return getString(R.string.tag_fish);
            case "tofu":        return getString(R.string.tag_tofu);
            case "milk":        return getString(R.string.tag_milk);
            case "tea":         return getString(R.string.tag_tea);
            case "cold":        return getString(R.string.tag_cold);
            case "hot":         return getString(R.string.tag_hot);
            case "refreshing":  return getString(R.string.tag_refreshing);
            case "classic":     return getString(R.string.tag_classic);
            case "traditional": return getString(R.string.tag_traditional);
            case "glutinous":   return getString(R.string.tag_glutinous);
            case "lemon":       return getString(R.string.tag_lemon);
            case "soda":        return getString(R.string.tag_soda);
            case "grape":       return getString(R.string.tag_grape);
            case "noodles":     return getString(R.string.tag_noodles);
            case "streetfood":  return getString(R.string.tag_streetfood);
            case "stirfry":     return getString(R.string.tag_stirfry);
            default:            return tag;
        }
    }
}