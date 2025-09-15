package com.example.fooddash;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FoodDetails extends AppCompatActivity {

    ImageView imageView, backButton;
    TextView itemName, itemPrice, itemRating, itemDescription;
    RatingBar ratingBar;
    ListView listView;
    static ArrayAdapter<String> adapter;
    static ArrayList<String> listItems = new ArrayList<>();

    String name, price, rating, imageURL, description;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_details);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        price = intent.getStringExtra("price");
        rating = intent.getStringExtra("rating");
        imageURL = intent.getStringExtra("image");
        description = intent.getStringExtra("description");

        imageView = findViewById(R.id.imageView5);
        itemName = findViewById(R.id.name);
        itemPrice = findViewById(R.id.price);
        itemRating = findViewById(R.id.rating);
        itemDescription = findViewById(R.id.foodDescription);
        ratingBar = findViewById(R.id.ratingBar);
        backButton = findViewById(R.id.imageView2);
        listView = findViewById(R.id.gallery1);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);

        Picasso.get()
                .load(imageURL)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(imageView);

        itemName.setText(name);
        itemPrice.setText(price);
        itemRating.setText(rating);
        itemDescription.setText(description);
        ratingBar.setRating(Float.parseFloat(rating));

        backButton.setOnClickListener(v -> {
            Intent backIntent = new Intent(getApplicationContext(), Home.class);
            backIntent.setAction("false");
            startActivity(backIntent);
        });

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    }

    public void shoppingCart(View v) {
        Intent intent = new Intent(this, ShoppingCart.class);
        startActivity(intent);
    }

    public void addToCart(View v) {
        if (price == null || name == null) return;

        executor.execute(() -> {
            try {
                double itemPrice = Double.parseDouble(price);
                String displayPrice = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(itemPrice);
                CartManager.addItem(name + "    " + displayPrice, itemPrice);

                CartManager.addItem(name, itemPrice);
                runOnUiThread(() -> {
                    listItems.clear();
                    listItems.addAll(CartManager.getDisplayItems());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(FoodDetails.this, "Added to cart", Toast.LENGTH_SHORT).show();
                });

            } catch (NumberFormatException e) {
                runOnUiThread(() ->
                        Toast.makeText(FoodDetails.this, "Invalid price format", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}