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
import com.example.yummyrestaurant.models.Order;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrdersAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_staff_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // If takeaway, show takeaway label instead of table number.
        if (order.getType().equals("takeaway")) {
            holder.tableNumber.setText(R.string.takeaway_label);
            holder.tableNumber.setTextColor(Color.RED);
        } else {
            holder.tableNumber.setText(order.getTableNumber());
            holder.tableNumber.setTextColor(Color.BLACK);
        }

        holder.orderSummary.setText(order.getSummary());

        // 瑷畾妯欑堡椤忚壊
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

        // 榛炴搳鏌ョ湅瑭虫儏 (鍚屾檪鎶婄洰鍓嶇殑鐙€鎱嬪偝閫插幓)
        holder.btnViewDetails.setOnClickListener(v -> {
            showOrderDetailsDialog(order, position);
        });
    }

    private void showOrderDetailsDialog(Order order, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Order #" + order.getOid());
        builder.setMessage("Loading details...");

        // ==============================================
        // 閫欒！灏辨槸銆岄毐钘忕増銆嶇殑鎿嶄綔閭忚集
        // ==============================================
        if (order.getStatus() == 1) {
            // 濡傛灉鏄?New锛屽彸涓嬭鎸夐垥椤ず "Start Cooking"
            builder.setPositiveButton("Start Cooking", (dialog, which) -> {
                updateOrderStatus(order, 2, position); // 鏀规垚 Cooking (2)
            });
        } else if (order.getStatus() == 2) {
            // 濡傛灉鏄?Cooking锛屽彸涓嬭鎸夐垥椤ず "Serve Order"
            builder.setPositiveButton("Serve / Done", (dialog, which) -> {
                updateOrderStatus(order, 3, position); // 鏀规垚 Delivered (3)
            });
        } else {
            // 宸茬稉瀹屾垚锛屽彧椤ず Close
            builder.setPositiveButton("Close", null);
        }

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // 杓夊叆瑭崇窗璩囨枡 (API)
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
                                msg.append("- ").append(items.getString(i)).append("\n");
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

    private void updateOrderStatus(Order order, int nextStatus, int position) {
        StringRequest request = new StringRequest(Request.Method.POST, ApiConstants.updateOrderStatus(),
                response -> {
                    Toast.makeText(context, "Order Updated!", Toast.LENGTH_SHORT).show();

                    // Remove the item from current filtered list after status transition.
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

