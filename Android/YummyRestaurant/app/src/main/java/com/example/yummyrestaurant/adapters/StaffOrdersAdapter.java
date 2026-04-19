package com.example.yummyrestaurant.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.models.StaffOrder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffOrdersAdapter extends RecyclerView.Adapter<StaffOrdersAdapter.OrderViewHolder> {

    private Context context;
    private List<StaffOrder> orderList;

    public StaffOrdersAdapter(Context context, List<StaffOrder> orderList) {
        this.context = context;
        this.orderList = orderList;
        android.util.Log.d("KitchenAdapter", "StaffOrdersAdapter created with " + orderList.size() + " orders");
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("KitchenAdapter", "Creating new ViewHolder");
        View view = LayoutInflater.from(context).inflate(R.layout.item_staff_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        StaffOrder order = orderList.get(position);
        android.util.Log.d("KitchenAdapter", "Binding order at position " + position + " - OID: " + order.getOid() + ", Status: " + order.getStatus());

        // 如果是外帶，桌號顯示為 "Takeaway" (紅色)，否則顯示桌號
        if (order.getType().equals("takeaway")) {
            holder.tableNumber.setText("🥡 TAKEAWAY");
            holder.tableNumber.setTextColor(Color.RED);
        } else {
            holder.tableNumber.setText(order.getTableNumber());
            holder.tableNumber.setTextColor(Color.BLACK);
        }

        holder.orderSummary.setText(order.getSummary());

        // 設定標籤顏色
        switch (order.getStatus()) {
            case 1: // New
                holder.orderStatus.setText("New");
                holder.orderStatus.setBackgroundColor(Color.parseColor("#D32F2F"));
                break;
            case 2: // Cooking
                holder.orderStatus.setText("Cooking");
                holder.orderStatus.setBackgroundColor(Color.parseColor("#FF9800"));
                break;
            case 3: // Delivered
                holder.orderStatus.setText("Delivered");
                holder.orderStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
        }

        // 點擊查看詳情 (同時把目前的狀態傳進去)
        holder.btnViewDetails.setOnClickListener(v -> {
            showOrderDetailsDialog(order, position);
        });
    }

    private void showOrderDetailsDialog(StaffOrder order, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Order #" + order.getOid());
        builder.setMessage("Loading details...");

        // ==============================================
        // 這裡就是「隱藏版」的操作邏輯
        // ==============================================
        if (order.getStatus() == 1) {
            // 如果是 New，右下角按鈕顯示 "Start Cooking"
            builder.setPositiveButton("Start Cooking", (dialog, which) -> {
                updateOrderStatus(order, 2, position); // 改成 Cooking (2)
            });
        } else if (order.getStatus() == 2) {
            // 如果是 Cooking，右下角按鈕顯示 "Serve Order"
            builder.setPositiveButton("Serve / Done", (dialog, which) -> {
                updateOrderStatus(order, 3, position); // 改成 Delivered (3)
            });
        } else {
            // 已經完成，只顯示 Close
            builder.setPositiveButton("Close", null);
        }

        // 左邊的取消按鈕
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // 載入詳細資料 (API)
        String url = ApiConstants.baseUrl() + "get_order_details_by_id.php?oid=" + order.getOid();

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            JSONObject data = json.getJSONObject("data");

                            String note = data.optString("note", "");
                            if (note.isEmpty() || note.equals("null")) note = "None";

                            StringBuilder msg = new StringBuilder();
                            msg.append("Customer: ").append(data.optString("customer", "Guest")).append("\n");
                            msg.append("Time: ").append(data.getString("time")).append("\n\n");
                            msg.append("=== ITEMS ===\n");

                            JSONArray items = data.getJSONArray("items");
                            for (int i=0; i<items.length(); i++) {
                                msg.append("• ").append(items.getString(i)).append("\n");
                            }

                            msg.append("\n=== NOTE ===\n");
                            msg.append(note);

                            dialog.setMessage(msg.toString());
                        } else {
                            dialog.setMessage("Failed to load details.");
                        }
                    } catch (Exception e) {
                        dialog.setMessage("Error: " + e.getMessage());
                    }
                },
                error -> dialog.setMessage("Network Error")
        );
        Volley.newRequestQueue(context).add(request);
    }

    private void updateOrderStatus(StaffOrder order, int nextStatus, int position) {
        StringRequest request = new StringRequest(Request.Method.POST, ApiConstants.updateOrderStatus(),
                response -> {
                    Toast.makeText(context, "Order Updated!", Toast.LENGTH_SHORT).show();

                    // 因為狀態變了 (例如從 New 變成 Cooking)，它不應該繼續留在 New 分頁
                    // 所以我們從目前的列表中移除它
                    if (position >= 0 && position < orderList.size()) {
                        orderList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, orderList.size());
                    }
                },
                error -> Toast.makeText(context, "Error updating", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("oid", String.valueOf(order.getOid()));
                params.put("status", String.valueOf(nextStatus));
                return params;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() {
        android.util.Log.d("KitchenAdapter", "getItemCount called - returning: " + orderList.size());
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tableNumber, orderTime, orderStatus, orderSummary;
        Button btnViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tableNumber = itemView.findViewById(R.id.tableNumber);
            orderTime = itemView.findViewById(R.id.orderTime);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderSummary = itemView.findViewById(R.id.orderSummary);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}