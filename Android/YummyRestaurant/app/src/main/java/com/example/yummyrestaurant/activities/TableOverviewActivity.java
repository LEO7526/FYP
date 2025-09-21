package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.api.TableApiService;
import com.example.yummyrestaurant.models.TableOrder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableOverviewActivity extends AppCompatActivity {

    private ListView tableListView;
    private ArrayAdapter<String> adapter;
    private List<TableOrder> tableOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_overview);

        tableListView = findViewById(R.id.tableListView);

        fetchTableOrders();
    }

    private void fetchTableOrders() {
        TableApiService service = RetrofitClient.getClient().create(TableApiService.class);
        Call<List<TableOrder>> call = service.getAllTableOrders();

        call.enqueue(new Callback<List<TableOrder>>() {
            @Override
            public void onResponse(Call<List<TableOrder>> call, Response<List<TableOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tableOrders = response.body();
                    displayTables();
                } else {
                    Toast.makeText(TableOverviewActivity.this, "Failed to load tables", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TableOrder>> call, Throwable t) {
                Log.e("TableOverview", "Error fetching tables", t);
                Toast.makeText(TableOverviewActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTables() {
        String[] tableLabels = new String[tableOrders.size()];
        for (int i = 0; i < tableOrders.size(); i++) {
            TableOrder order = tableOrders.get(i);
            tableLabels[i] = "Table " + order.getTableNumber() + " — " + order.getStatus();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tableLabels);
        tableListView.setAdapter(adapter);

        tableListView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            TableOrder selectedOrder = tableOrders.get(position);
            Intent intent = new Intent(TableOverviewActivity.this, TableOrderDetailActivity.class);
            intent.putExtra("table_order_id", selectedOrder.getToid());
            intent.putExtra("order_id", selectedOrder.getOid());
            startActivity(intent);
        });
    }
}