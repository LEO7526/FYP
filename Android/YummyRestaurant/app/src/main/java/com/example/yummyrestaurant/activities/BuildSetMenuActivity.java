package com.example.yummyrestaurant.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.SelectableMenuItemAdapter;
import com.example.yummyrestaurant.api.MenuApi;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.models.OrderItemCustomization;
import com.example.yummyrestaurant.models.PackageType;
import com.example.yummyrestaurant.models.SetMenu;
import com.example.yummyrestaurant.models.SetMenuResponse;
import com.example.yummyrestaurant.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuildSetMenuActivity extends AppCompatActivity {

    private LinearLayout packageContainer;
    private Button confirmBtn;
    private final List<SelectableMenuItemAdapter> adapters = new ArrayList<>();
    private SetMenu currentSetMenu;
    private boolean isReorder = false;
    private int prefillPackageId = -1;
    private static final int CUSTOMIZE_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_set_menu);

        packageContainer = findViewById(R.id.packageContainer);
        confirmBtn = findViewById(R.id.confirmSetMenuBtn);

        int packageId = getIntent().getIntExtra("package_id", 1); // default to 1 if not passed
        isReorder = getIntent().getBooleanExtra("is_reorder", false);
        prefillPackageId = packageId;

        MenuApi menuApi = RetrofitClient.getClient(this).create(MenuApi.class);
        Call<SetMenuResponse> call = menuApi.getSetMenu(packageId, "en");

        call.enqueue(new Callback<SetMenuResponse>() {
            @Override
            public void onResponse(Call<SetMenuResponse> call, Response<SetMenuResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentSetMenu = response.body().getData();
                    if (currentSetMenu != null) {
                        Log.d("BuildSetMenuActivity", "Loaded SetMenu: " + currentSetMenu.getName());
                        setupAdaptersFromPackage(currentSetMenu);
                    } else {
                        Log.w("BuildSetMenuActivity", "SetMenu data is null");
                        Toast.makeText(BuildSetMenuActivity.this, "No menu data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("BuildSetMenuActivity", "API failed: " + response.code());
                    Toast.makeText(BuildSetMenuActivity.this, "Failed to load set menu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SetMenuResponse> call, Throwable t) {
                Log.e("BuildSetMenuActivity", "API error", t);
                Toast.makeText(BuildSetMenuActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAdaptersFromPackage(SetMenu setMenu) {
        packageContainer.removeAllViews();
        adapters.clear();

        if (setMenu == null) {
            Log.e("BuildSetMenuActivity", "SetMenu is null!");
            return;
        }

        Log.d("BuildSetMenuActivity", "SetMenu loaded: id=" + setMenu.getId()
                + ", name=" + setMenu.getName()
                + ", numOfType=" + setMenu.getNumOfType()
                + ", discount=" + setMenu.getDiscount());

        List<PackageType> types = setMenu.getTypes();
        if (types == null || types.isEmpty()) {
            Log.w("BuildSetMenuActivity", "No types found in SetMenu");
            return;
        }

        for (PackageType type : types) {
            Log.d("BuildSetMenuActivity", "Type: id=" + type.getId()
                    + ", name=" + type.getName()
                    + ", optionalQuantity=" + type.getOptionalQuantity()
                    + ", items=" + (type.getItems() != null ? type.getItems().size() : 0));

            // 1. Create label
            TextView label = new TextView(this);
            label.setText("Choose " + type.getOptionalQuantity() + " " + type.getName());
            label.setTextSize(16f);
            label.setTypeface(null, Typeface.BOLD);
            label.setPadding(0, 24, 0, 8);
            packageContainer.addView(label);

            // 2. Create RecyclerView
            RecyclerView rv = new RecyclerView(this);
            rv.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.recycler_height)
            ));
            rv.setNestedScrollingEnabled(false);

            // 3. Setup adapter
            SelectableMenuItemAdapter adapter = new SelectableMenuItemAdapter(
                    type.getItems(),
                    type.getOptionalQuantity()
            );
            
            // Set customization listener
            adapter.setOnCustomizeClickListener(item -> {
                Intent intent = new Intent(BuildSetMenuActivity.this, CustomizeDishActivity.class);
                intent.putExtra(CustomizeDishActivity.EXTRA_MENU_ITEM, item);
                intent.putExtra(CustomizeDishActivity.EXTRA_QUANTITY, 1);
                intent.putExtra("FROM_PACKAGE", true);
                startActivityForResult(intent, CUSTOMIZE_REQUEST_CODE);
            });
            
            rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rv.setAdapter(adapter);

            // 4. 應用預填數據（如果是 Reorder）
            if (isReorder && prefillPackageId > 0) {
                List<MenuItem> prefillItems = CartManager.getPrefillPackageData(prefillPackageId);
                if (prefillItems != null && !prefillItems.isEmpty()) {
                    adapter.preselectItems(prefillItems);
                    Log.d("BuildSetMenuActivity", "Applied prefill data: " + prefillItems.size() + " items");
                }
            }

            // 5. Add to container and keep reference
            packageContainer.addView(rv);
            adapters.add(adapter);
        }

        confirmBtn.setOnClickListener(v -> confirmSelection());
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CUSTOMIZE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            MenuItem customizedItem = (MenuItem) data.getSerializableExtra("customized_item");
            if (customizedItem != null) {
                Log.d("BuildSetMenuActivity", "Received customized item: " + customizedItem.getName() + 
                      " with " + (customizedItem.getCustomizations() != null ? customizedItem.getCustomizations().size() : 0) + " customizations");
                
                boolean updated = false;
                // Find and update the item in the adapter's internal list with its customizations
                for (SelectableMenuItemAdapter adapter : adapters) {
                    if (adapter.updateItemCustomizations(customizedItem.getId(), customizedItem.getCustomizations())) {
                        Log.d("BuildSetMenuActivity", "Updated customizations for item: " + customizedItem.getName());
                        updated = true;
                        break;
                    }
                }
                
                if (updated) {
                    Toast.makeText(this, "Customization saved", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w("BuildSetMenuActivity", "Could not find item to update: " + customizedItem.getName());
                }
            }
        }
    }
    
    private void confirmSelection() {
        // Collect all chosen items from each adapter
        final List<MenuItem> allChosen = new ArrayList<>();

        for (SelectableMenuItemAdapter adapter : adapters) {
            List<MenuItem> selected = adapter.getSelectedItems();
            if (selected.size() < adapter.getRequiredCount()) {
                Toast.makeText(this, "Please complete your selection", Toast.LENGTH_SHORT).show();
                return;
            }
            allChosen.addAll(selected);
        }

        // Calculate totals
        double total = 0.0;
        for (MenuItem m : allChosen) {
            total += m.getPrice();
        }
        final double finalTotal = total;

        // Use discount from DB if available, otherwise fallback
        double discountRate = currentSetMenu.getDiscount() > 0 ? currentSetMenu.getDiscount() : 1.0;
        final double discountedPrice = finalTotal * discountRate;

        // Build summary string with customization details
        StringBuilder summary = new StringBuilder();
        for (MenuItem item : allChosen) {
            summary.append("• ")
                    .append(item.getName() != null ? item.getName() : "Unnamed Dish")
                    .append("  $")
                    .append(String.format("%.2f", item.getPrice()))
                    .append("\n");
            
            // ✅ Add customization details if present
            if (item.getCustomizations() != null && !item.getCustomizations().isEmpty()) {
                for (OrderItemCustomization custom : item.getCustomizations()) {
                    summary.append("  → ").append(custom.getDisplayText()).append("\n");
                }
            }
        }

        summary.append("\nOriginal Total: $").append(String.format("%.2f", finalTotal))
                .append("\nDiscounted Total: $").append(String.format("%.2f", discountedPrice));

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Your " + currentSetMenu.getName())
                .setMessage(summary.toString())
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // ✅ Add package item to cart (as a special marker item)
                    // Store package ID and selected items in a custom CartItem
                    MenuItem packageMarker = new MenuItem();
                    packageMarker.setId(currentSetMenu.getId());
                    packageMarker.setName(currentSetMenu.getName());
                    packageMarker.setPrice(discountedPrice);
                    packageMarker.setCategory("PACKAGE");
                    
                    // Add marker item to cart (qty=1 represents the whole package)
                    CartItem packageItem = new CartItem(packageMarker, null);
                    CartManager.addItem(packageItem, 1);
                    
                    // Store the package details in CartManager for later use
                    CartManager.setPackageDetails(currentSetMenu.getId(), allChosen, discountedPrice);
                    
                    // 清除預填數據（Reorder 完成）
                    CartManager.clearPrefillPackageData(currentSetMenu.getId());

                    Toast.makeText(
                            BuildSetMenuActivity.this,
                            currentSetMenu.getName() + " added! You saved HK$" +
                                    String.format("%.2f", finalTotal - discountedPrice),
                            Toast.LENGTH_LONG
                    ).show();

                    // ✅ Navigate to CartActivity instead of finish
                    Intent intent = new Intent(BuildSetMenuActivity.this, CartActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}