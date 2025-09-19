package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.MenuApi;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.adapters.MenuItemAdapter;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.MenuResponse;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    MenuItemAdapter adapter;
    String currentLanguage = "en"; // Default language

    Spinner categorySpinner;
    Spinner spiceSpinner;
    Spinner tagSpinner;

    ProgressBar loadingSpinner;

    EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        categorySpinner = findViewById(R.id.categorySpinner);
        spiceSpinner = findViewById(R.id.spiceSpinner);
        tagSpinner = findViewById(R.id.tagSpinner);

        searchBar = findViewById(R.id.searchBar);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.search(s.toString()); // Call your adapter's search method
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        String[] categories = {"All", "Appetizers", "Main Courses"};
        String[] spiceLevels = {"All", "Mild", "Numbing"};
        String[] tags = {"All", "vegetarian", "refreshing", "beef", "spicy"};

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        ArrayAdapter<String> spiceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spiceLevels);
        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tags);

        categorySpinner.setAdapter(categoryAdapter);
        spiceSpinner.setAdapter(spiceAdapter);
        tagSpinner.setAdapter(tagAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters(categorySpinner, spiceSpinner, tagSpinner);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        categorySpinner.setOnItemSelectedListener(filterListener);
        spiceSpinner.setOnItemSelectedListener(filterListener);
        tagSpinner.setOnItemSelectedListener(filterListener);

        loadingSpinner = findViewById(R.id.loadingSpinner);

        adapter = new MenuItemAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);


        Log.d("TestLog", "MenuActivity started");

        loadMenuItemsFromServer();

    }

    private void loadMenuItemsFromServer() {
        Log.d("MenuActivity", "Loading menu items for language: " + currentLanguage);
        loadingSpinner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        MenuApi menuApi = RetrofitClient.getClient().create(MenuApi.class);
        Call<MenuResponse> call = menuApi.getMenuItems(currentLanguage);

        call.enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                loadingSpinner.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    // 🔍 Log full deserialized response
                    Log.d("RawResponse", new Gson().toJson(response.body()));

                    adapter.setMenuItems(response.body().data);
                    applyFilters(categorySpinner, spiceSpinner, tagSpinner);


                    // 🔍 Log each item individually
                    for (MenuItem item : response.body().data) {
                        Log.d("MenuItem", "ID: " + item.getId() +
                                ", Name: " + item.getName() +
                                ", Image URL: " + item.getImage_url() +
                                ", Description: " + item.getDescription() +
                                ", Price: " + item.getPrice());
                    }

                    //log the first one
                    MenuItem first = response.body().data.get(0);
                    Log.d("Debug123", "Raw name: " + first.getImage_url());

                } else {
                    Log.e("MenuActivity", "Response failed or empty");
                    Toast.makeText(MenuActivity.this, "Failed to load menu items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Log.e("MenuActivity", "API call failed: " + t.getMessage());
                Toast.makeText(MenuActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters(Spinner categorySpinner, Spinner spiceSpinner, Spinner tagSpinner) {
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String selectedSpice = spiceSpinner.getSelectedItem().toString();
        String selectedTag = tagSpinner.getSelectedItem().toString();
        adapter.filter(selectedCategory, selectedSpice, selectedTag);
    }
}