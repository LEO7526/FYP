package com.example.fooddash;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.ArrayAdapter;

import java.util.Locale;

public class ShoppingCart extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> adapter;
    ImageView backButton;
    TextView totalPrice;
    private String deliveryTime; // Store duration once

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        // Retrieve delivery time from intent
        deliveryTime = getIntent().getStringExtra("duration");

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Home.class);
            intent.setAction("false");
            startActivity(intent);
        });

        listView = findViewById(R.id.userList);
        totalPrice = findViewById(R.id.totalCartPrice);
        totalPrice.setText("Total: $" + String.format(Locale.US, "%.2f", CartManager.getTotal()));

        adapter = FoodDetails.adapter;
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage("Would you like to remove this item?")
                    .setPositiveButton("OK", (dialog, id) -> {
                        String itemText = adapterView.getItemAtPosition(i).toString();
                        String itemName = itemText.split(" x")[0].trim();
                        CartManager.removeItem(itemName);

                        FoodDetails.listItems.clear();
                        FoodDetails.listItems.addAll(CartManager.getDisplayItems());
                        adapter.notifyDataSetChanged();
                        totalPrice.setText("Total: $" + String.format(Locale.US, "%.2f", CartManager.getTotal()));
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

            builder.show();
        });
    }

    public void confirmButton(android.view.View view) {
        if (FoodDetails.listItems.size() != 0) {
            String formattedDuration = (deliveryTime != null && !deliveryTime.isEmpty())
                    ? "⏱ Estimated arrival: " + deliveryTime
                    : "⏱ Estimated arrival time unavailable";

            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage("🚚 The delivery is on its way!\n\n" + formattedDuration)
                    .setNeutralButton("OK", (dialog, id) -> {
                        dialog.dismiss();
                        CartManager.clearCart();
                        FoodDetails.listItems.clear();
                        adapter.notifyDataSetChanged();
                        totalPrice.setText("Total: $0.00");
                        finish();
                    });

            AlertDialog dialog = builder.create();
            dialog.show();

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1.0f;
            layoutParams.gravity = Gravity.CENTER;
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setLayoutParams(layoutParams);

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage("Your cart is empty. Please add an item.")
                    .setNeutralButton("OK", (dialog, id) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1.0f;
            layoutParams.gravity = Gravity.CENTER;
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setLayoutParams(layoutParams);
        }
    }
}