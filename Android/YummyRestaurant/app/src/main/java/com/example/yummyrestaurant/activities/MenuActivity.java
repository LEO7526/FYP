package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.MenuApi;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.adapters.MenuItemAdapter;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.MenuResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.ProgressBar;

public class MenuActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    MenuItemAdapter adapter;
    List<MenuItem> menuItemList = new ArrayList<>();
    String currentLanguage = "en"; // Change this dynamically based on user preference

    ProgressBar loadingSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadingSpinner = findViewById(R.id.loadingSpinner);
        adapter = new MenuItemAdapter(menuItemList, currentLanguage);
        recyclerView.setAdapter(adapter);

        loadMenuItemsFromServer();

        Spinner languageSpinner = findViewById(R.id.languageSpinner);
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentLanguage = "en"; break;
                    case 1: currentLanguage = "zh-CN"; break;
                    case 2: currentLanguage = "zh-TW"; break;
                }
                loadMenuItemsFromServer(); // Reload with selected language
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadMenuItemsFromServer() {
        loadingSpinner.setVisibility(View.VISIBLE);      // Show spinner
        recyclerView.setVisibility(View.GONE);           // Hide list

        MenuApi menuApi = RetrofitClient.getClient().create(MenuApi.class);
        Call<MenuResponse> call = menuApi.getMenuItems(currentLanguage);

        call.enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                loadingSpinner.setVisibility(View.GONE);  // Hide spinner
                recyclerView.setVisibility(View.VISIBLE); // Show list

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    menuItemList.clear();
                    menuItemList.addAll(response.body().data);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MenuActivity.this, "Failed to load menu items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);  // Hide spinner
                recyclerView.setVisibility(View.VISIBLE); // Show list

                Toast.makeText(MenuActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}