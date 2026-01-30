package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.yummyrestaurant.R;
import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.yummyrestaurant.R;
import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends ThemeBaseActivity {
    ListView wishlistView;
    ArrayAdapter<String> adapter;
    List<String> wishlistItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        wishlistView = findViewById(R.id.wishlistView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wishlistItems);
        wishlistView.setAdapter(adapter);

        // 示例：添加项目到愿望清单
        wishlistItems.add("Spicy Ramen");
        adapter.notifyDataSetChanged();
    }
}


