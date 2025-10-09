package com.example.yummyrestaurant.activities;

import android.app.AlertDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_set_menu);

        packageContainer = findViewById(R.id.packageContainer);
        confirmBtn = findViewById(R.id.confirmSetMenuBtn);

        int packageId = getIntent().getIntExtra("package_id", 1); // default to 1 if not passed

        MenuApi menuApi = RetrofitClient.getClient().create(MenuApi.class);
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
            rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rv.setAdapter(adapter);

            // 4. Add to container and keep reference
            packageContainer.addView(rv);
            adapters.add(adapter);
        }

        confirmBtn.setOnClickListener(v -> confirmSelection());
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

        // Build summary string
        StringBuilder summary = new StringBuilder();
        for (MenuItem item : allChosen) {
            summary.append("• ")
                    .append(item.getName() != null ? item.getName() : "Unnamed Dish")
                    .append("  $")
                    .append(String.format("%.2f", item.getPrice()))
                    .append("\n");
        }

        summary.append("\nOriginal Total: $").append(String.format("%.2f", finalTotal))
                .append("\nDiscounted Total: $").append(String.format("%.2f", discountedPrice));

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Your " + currentSetMenu.getName())
                .setMessage(summary.toString())
                .setPositiveButton("Confirm", (dialog, which) -> {
                    for (MenuItem item : allChosen) {
                        CartItem cartItem = new CartItem(item, null);
                        CartManager.addItem(cartItem, 1);
                    }

                    Toast.makeText(
                            BuildSetMenuActivity.this,
                            currentSetMenu.getName() + " added! You saved $" +
                                    String.format("%.2f", finalTotal - discountedPrice),
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}