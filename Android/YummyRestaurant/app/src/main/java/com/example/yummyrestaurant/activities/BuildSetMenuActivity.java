package com.example.yummyrestaurant.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
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
import com.example.yummyrestaurant.models.MenuResponse;
import com.example.yummyrestaurant.models.SetMenu;
import com.example.yummyrestaurant.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuildSetMenuActivity extends AppCompatActivity {

    private RecyclerView appetizerRecycler, soupRecycler, mainsRecycler, dessertRecycler, drinkRecycler;
    private Button confirmBtn;

    private static final double DISCOUNT_RATE = 0.10; // 10% off

    private SelectableMenuItemAdapter appetizerAdapter, soupAdapter, mainsAdapter, dessertAdapter, drinkAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_set_menu);

        appetizerRecycler = findViewById(R.id.appetizerRecycler);
        soupRecycler = findViewById(R.id.soupRecycler);
        mainsRecycler = findViewById(R.id.mainsRecycler);
        dessertRecycler = findViewById(R.id.dessertRecycler);
        drinkRecycler = findViewById(R.id.drinkRecycler);
        confirmBtn = findViewById(R.id.confirmSetMenuBtn);

        // Fetch menu items from server
        MenuApi menuApi = RetrofitClient.getClient().create(MenuApi.class);
        Call<MenuResponse> call = menuApi.getMenuItems("en");

        call.enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<MenuItem> allItems = response.body().data;

                    // Filter into categories
                    List<MenuItem> appetizers = filterByCategory(allItems, "Appetizers");
                    List<MenuItem> soups = filterByCategory(allItems, "Soup");
                    List<MenuItem> mains = filterByCategory(allItems, "Main Courses");
                    List<MenuItem> desserts = filterByCategory(allItems, "Dessert");
                    List<MenuItem> drinks = filterByCategory(allItems, "Drink");

                    // Setup adapters
                    appetizerAdapter = new SelectableMenuItemAdapter(appetizers, 1); // single
                    soupAdapter = new SelectableMenuItemAdapter(soups, 1); // single
                    mainsAdapter = new SelectableMenuItemAdapter(mains, 2); // allow 2
                    dessertAdapter = new SelectableMenuItemAdapter(desserts, 1);
                    drinkAdapter = new SelectableMenuItemAdapter(drinks, 1);

                    setupRecycler(appetizerRecycler, appetizerAdapter);
                    setupRecycler(soupRecycler, soupAdapter);
                    setupRecycler(mainsRecycler, mainsAdapter);
                    setupRecycler(dessertRecycler, dessertAdapter);
                    setupRecycler(drinkRecycler, drinkAdapter);

                    confirmBtn.setOnClickListener(v -> confirmSelection());
                } else {
                    Toast.makeText(BuildSetMenuActivity.this, "Failed to load menu items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                Toast.makeText(BuildSetMenuActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecycler(RecyclerView rv, RecyclerView.Adapter adapter) {
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);
    }

    private List<MenuItem> filterByCategory(List<MenuItem> items, String category) {
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : items) {
            if (item.getCategory() != null && item.getCategory().equalsIgnoreCase(category)) {
                result.add(item);
            }
        }
        return result;
    }

    private void confirmSelection() {
        List<MenuItem> chosenApp = appetizerAdapter.getSelectedItems();
        List<MenuItem> chosenSoup = soupAdapter.getSelectedItems();
        List<MenuItem> chosenMains = mainsAdapter.getSelectedItems();
        List<MenuItem> chosenDessert = dessertAdapter.getSelectedItems();
        List<MenuItem> chosenDrink = drinkAdapter.getSelectedItems();

        if (chosenApp.size() != 1 || chosenSoup.size() != 1 ||
                chosenMains.size() != 2 || chosenDessert.size() != 1 || chosenDrink.size() != 1) {
            Toast.makeText(this, "Please complete your set menu selection", Toast.LENGTH_SHORT).show();
            return;
        }

        final List<MenuItem> allChosen = new ArrayList<>();
        allChosen.addAll(chosenApp);
        allChosen.addAll(chosenSoup);
        allChosen.addAll(chosenMains);
        allChosen.addAll(chosenDessert);
        allChosen.addAll(chosenDrink);

        // Calculate total price
        double total = 0.0;
        for (MenuItem m : allChosen) {
            total += m.getPrice();
        }
        final double totalPrice = total;

        // Apply discount (example: 10%)

        final double discountedPrice = totalPrice * (1 - DISCOUNT_RATE);

        // Build a string with all selected dish names + prices
        StringBuilder summary = new StringBuilder();
        for (MenuItem item : allChosen) {
            summary.append("• ")
                    .append(item.getName() != null ? item.getName() : "Unnamed Dish")
                    .append("  $")
                    .append(String.format("%.2f", item.getPrice()))
                    .append("\n");
        }

        // Add totals at the bottom
        summary.append("\nOriginal Total: $").append(String.format("%.2f", totalPrice))
                .append("\nDiscounted Total: $").append(String.format("%.2f", discountedPrice));

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Your Set Menu")
                .setMessage(summary.toString())
                .setPositiveButton("Confirm", (dialog, which) -> {
                    SetMenu customMenu = new SetMenu(
                            "Custom Menu",
                            "Your own combination",
                            discountedPrice,
                            allChosen
                    );

                    // Add to cart
                    for (MenuItem item : customMenu.getItems()) {
                        CartItem cartItem = new CartItem(item, null);
                        CartManager.addItem(cartItem, 1);
                    }

                    Toast.makeText(this,
                            "Custom Menu added! You saved $" + String.format("%.2f", totalPrice - discountedPrice),
                            Toast.LENGTH_LONG).show();

                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}