package com.example.yummyrestaurant.activities;

import com.example.yummyrestaurant.adapters.DishListAdapter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import com.example.yummyrestaurant.utils.LanguageManager;

public class DishManagementActivity extends StaffBaseActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private DishListAdapter adapter;
    private List<JSONObject> dishList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_management);

        recyclerView = findViewById(R.id.dishRecyclerView);
        progressBar = findViewById(R.id.dishProgressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DishListAdapter(this, dishList, id -> {
            Log.d("DishManagement", "Clicked dish id: " + id);
        });
        recyclerView.setAdapter(adapter);
        loadDishList();
    }

    private void loadDishList() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "http://10.0.2.2/projectapi/get_dish_list.php?lang=" + LanguageManager.getLangCode(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d("DishManagement", "API response: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optBoolean("success", false)) {
                            JSONArray data = json.optJSONArray("data");
                            dishList.clear();
                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    dishList.add(data.getJSONObject(i));
                                }
                            }
                            Log.d("DishManagement", "dishList size: " + dishList.size());
                            adapter.setData(dishList);
                        } else {
                            Log.e("DishManagement", "API error: " + json.optString("message"));
                        }
                    } catch (Exception e) {
                        Log.e("DishManagement", "Parsing error", e);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("DishManagement", "Network error", error);
                }
        );
        Volley.newRequestQueue(this).add(request);
    }
}
