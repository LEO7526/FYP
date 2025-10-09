package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.PackagesAdapter;
import com.example.yummyrestaurant.api.MenuApi;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.PackagesResponse;
import com.example.yummyrestaurant.models.SetMenu;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PackagesActivity extends AppCompatActivity implements PackagesAdapter.OnPackageClickListener {

    private RecyclerView recyclerView;
    private PackagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packages);

        recyclerView = findViewById(R.id.packagesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadPackages();
    }

    private void loadPackages() {
        MenuApi menuApi = RetrofitClient.getClient().create(MenuApi.class);
        Call<PackagesResponse> call = menuApi.getPackages();

        call.enqueue(new Callback<PackagesResponse>() {
            @Override
            public void onResponse(Call<PackagesResponse> call, Response<PackagesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<SetMenu> packages = response.body().getData();
                    adapter = new PackagesAdapter(packages, PackagesActivity.this);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(PackagesActivity.this, "Failed to load packages", Toast.LENGTH_SHORT).show();
                    Log.e("PackagesActivity", "API error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PackagesResponse> call, Throwable t) {
                Toast.makeText(PackagesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("PackagesActivity", "API failure", t);
            }
        });
    }

    @Override
    public void onPackageClick(SetMenu setMenu) {
        Intent intent = new Intent(this, BuildSetMenuActivity.class);
        intent.putExtra("package_id", setMenu.getId());
        startActivity(intent);
    }
}