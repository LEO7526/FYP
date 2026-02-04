package com.example.yummyrestaurant.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.TablesAdapter;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.models.Table;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TableSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TablesAdapter adapter;
    private List<Table> tableList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_select_table);

        // 1. 綁定返回按鈕
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 2. 初始化列表與 Adapter
        tableList = new ArrayList<>();
        recyclerView = findViewById(R.id.tablesRecyclerView);

        // 設定網格佈局 (每行顯示 3 個桌子)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new TablesAdapter(this, tableList);
        recyclerView.setAdapter(adapter);

        // 3. 載入桌位狀態
        fetchTableStatus();
    }

    private void fetchTableStatus() {
        String url = ApiConstants.BASE_URL + "get_table_status.php";
        
        android.util.Log.d("TableSelection", "Fetching table status from: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    android.util.Log.d("TableSelection", "API Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            tableList.clear();

                            android.util.Log.d("TableSelection", "Number of tables received: " + data.length());

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                Table table = new Table(
                                        obj.getInt("id"),
                                        obj.getInt("status"),
                                        obj.getString("status_text")
                                );
                                tableList.add(table);
                                android.util.Log.d("TableSelection", "Added table - ID: " + table.getId() + ", Status: " + table.getStatus() + ", Text: " + table.getStatusText());
                            }
                            adapter.notifyDataSetChanged();
                            android.util.Log.d("TableSelection", "Table list updated with " + tableList.size() + " tables");
                        } else {
                            String errorMessage = jsonObject.optString("message", "Unknown error");
                            android.util.Log.e("TableSelection", "API Error: " + errorMessage);
                            Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("TableSelection", "JSON parsing error", e);
                        Toast.makeText(this, "Data parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    android.util.Log.e("TableSelection", "Network error", error);
                    Toast.makeText(this, "Network Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    public void onTableClicked(int tableId) {
        showTableDetails(tableId);
    }

    // 顯示詳情 (已移除對 detailStatus 的依賴)
    private void showTableDetails(int tableId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_table_details, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // 1. 綁定 UI (注意：這裡不抓取 detailStatus，因為 XML 已移除)
        TextView title = view.findViewById(R.id.detailTableTitle);
        TextView customer = view.findViewById(R.id.detailCustomer);
        TextView time = view.findViewById(R.id.detailTime);
        TextView items = view.findViewById(R.id.detailItems);
        Button btnClose = view.findViewById(R.id.btnCloseDetail);

        // 2. 初始化
        title.setText("Table " + tableId);
        customer.setText("Loading...");
        time.setText("Loading...");
        items.setText("Checking data...");

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        // 3. API 請求詳情
        String url = ApiConstants.BASE_URL + "get_table_detail.php?tid=" + tableId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.has("status") || !json.getString("status").equals("success")) {
                            items.setText("No data available.");
                            return;
                        }

                        JSONObject data = json.optJSONObject("data");
                        if (data == null) return;

                        String type = data.optString("type", "unknown");
                        String name = data.optString("customer_name", "None");
                        customer.setText(name);

                        // 判斷狀態類型並顯示
                        if (type.equals("live")) {
                            // 現場用餐中
                            int min = data.optInt("duration", 0);
                            time.setText(min > 1440 ? (min/1440) + " days ago" : min + " mins");

                            JSONArray itemArray = data.optJSONArray("items");
                            if (itemArray != null && itemArray.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < itemArray.length(); i++) {
                                    sb.append("• ").append(itemArray.optString(i)).append("\n");
                                }
                                items.setText(sb.toString());
                            } else {
                                items.setText("No items ordered yet.");
                            }
                        } else if (type.equals("booking")) {
                            // 預約單
                            int pax = data.optInt("pnum", 0);
                            time.setText(pax + " Guests (Booked)");
                            items.setText("Note: " + data.optString("remark", "No remark."));
                        } else {
                            // 空桌
                            time.setText("-");
                            items.setText("Table is currently available.");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        items.setText("Error parsing data.");
                    }
                },
                error -> items.setText("Net Error: " + error.getMessage())
        );
        Volley.newRequestQueue(this).add(request);
    }
}