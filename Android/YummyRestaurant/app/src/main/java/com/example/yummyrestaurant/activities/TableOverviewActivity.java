package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.TableGridAdapter;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.api.TableApiService;
import com.example.yummyrestaurant.models.TableOrder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableOverviewActivity extends AppCompatActivity {

    private List<TableOrder> tableOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_overview);

        fetchTableOrders(); // Adapter setup happens after data is loaded
    }

    private void fetchTableOrders() {
        TableApiService service = RetrofitClient.getClient(this).create(TableApiService.class);
        Call<List<TableOrder>> call = service.getAllTableOrders();

        call.enqueue(new Callback<List<TableOrder>>() {
            @Override
            public void onResponse(Call<List<TableOrder>> call, Response<List<TableOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tableOrders = response.body();

                    GridView tableGridView = findViewById(R.id.tableGridView);
                    TableGridAdapter adapter = new TableGridAdapter(TableOverviewActivity.this, tableOrders);
                    tableGridView.setAdapter(adapter);

                    tableGridView.setOnItemClickListener((parent, view, position, id) -> {
                        TableOrder selectedOrder = tableOrders.get(position);
                        String status = selectedOrder.getStatus();

                        switch (status) {
                            case "available":
                                Toast.makeText(TableOverviewActivity.this, "Table is available. No action needed.", Toast.LENGTH_SHORT).show();
                                break;
                            case "reserved":
                                Toast.makeText(TableOverviewActivity.this, "Table is reserved. Waiting for guests.", Toast.LENGTH_SHORT).show();
                                break;
                            case "seated":
                                Toast.makeText(TableOverviewActivity.this, "Guests are seated. Ready to take order.", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Intent intent = new Intent(TableOverviewActivity.this, TableOrderDetailActivity.class);
                                intent.putExtra("table_order_id", selectedOrder.getToid());
                                intent.putExtra("order_id", selectedOrder.getOid());
                                startActivity(intent);
                                break;
                        }
                    });

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
}