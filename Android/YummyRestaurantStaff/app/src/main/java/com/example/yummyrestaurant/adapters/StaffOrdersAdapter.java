package com.example.yummyrestaurant.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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
import java.util.Locale;
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

        // If takeaway, show takeaway label instead of table number.
        if (order.getType().equals("takeaway")) {
            holder.tableNumber.setText(context.getString(R.string.takeaway_label));
            holder.tableNumber.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            holder.tableNumber.setText(order.getTableNumber());
            holder.tableNumber.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        }

        holder.orderSummary.setText(order.getSummary());
        holder.orderTime.setText(formatOrderTime(order.getOrderTime()));


        // 瑷畾妯欑堡椤忚壊
        switch (order.getStatus()) {
            case 1: // New
                holder.orderStatus.setText(R.string.order_status_new);
                holder.orderStatus.setBackgroundColor(Color.parseColor("#E53935"));
                holder.statusAccent.setBackgroundColor(Color.parseColor("#E53935"));
                holder.orderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                break;
            case 2: // Cooking
                holder.orderStatus.setText(R.string.order_status_cooking);
                holder.orderStatus.setBackgroundColor(Color.parseColor("#FB8C00"));
                holder.statusAccent.setBackgroundColor(Color.parseColor("#FB8C00"));
                holder.orderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                break;
            case 3: // Delivered
                holder.orderStatus.setText(R.string.order_status_delivered);
                holder.orderStatus.setBackgroundColor(Color.parseColor("#43A047"));
                holder.statusAccent.setBackgroundColor(Color.parseColor("#43A047"));
                holder.orderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                break;
            default:
                holder.orderStatus.setText(R.string.order_status_new);
                holder.orderStatus.setBackgroundColor(Color.parseColor("#1E88E5"));
                holder.statusAccent.setBackgroundColor(Color.parseColor("#1E88E5"));
                holder.orderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                break;
        }

        // Primary action button for kitchen workflow
        LinearLayout.LayoutParams detailParams = (LinearLayout.LayoutParams) holder.btnViewDetails.getLayoutParams();

        if (order.getStatus() == 1) {
            holder.btnPrimaryAction.setVisibility(View.VISIBLE);
            holder.btnPrimaryAction.setText(R.string.start_cooking);
            holder.btnPrimaryAction.setOnClickListener(v -> updateOrderStatus(order, 2, holder.getBindingAdapterPosition()));
            detailParams.leftMargin = dpToPx(10);
            holder.btnViewDetails.setLayoutParams(detailParams);
        } else if (order.getStatus() == 2) {
            holder.btnPrimaryAction.setVisibility(View.VISIBLE);
            holder.btnPrimaryAction.setText(R.string.serve_done);
            holder.btnPrimaryAction.setOnClickListener(v -> updateOrderStatus(order, 3, holder.getBindingAdapterPosition()));
            detailParams.leftMargin = dpToPx(10);
            holder.btnViewDetails.setLayoutParams(detailParams);
        } else {
            holder.btnPrimaryAction.setVisibility(View.GONE);
            detailParams.leftMargin = 0;
            holder.btnViewDetails.setLayoutParams(detailParams);
        }

        // 榛炴搳鏌ョ湅瑭虫儏 (鍚屾檪鎶婄洰鍓嶇殑鐙€鎱嬪偝閫插幓)
        holder.btnViewDetails.setOnClickListener(v -> {
            showOrderDetailsDialog(order, position);
        });
    }

    private void showOrderDetailsDialog(StaffOrder order, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.order_number_title, order.getOid()));
        builder.setMessage(context.getString(R.string.loading_details));

        // ==============================================
        // 閫欒！灏辨槸銆岄毐钘忕増銆嶇殑鎿嶄綔閭忚集
        // ==============================================
        if (order.getStatus() == 1) {
            // 濡傛灉鏄?New锛屽彸涓嬭鎸夐垥椤ず "Start Cooking"
            builder.setPositiveButton(R.string.start_cooking, (dialog, which) -> {
                updateOrderStatus(order, 2, position); // 鏀规垚 Cooking (2)
            });
        } else if (order.getStatus() == 2) {
            // 濡傛灉鏄?Cooking锛屽彸涓嬭鎸夐垥椤ず "Serve Order"
            builder.setPositiveButton(R.string.serve_done, (dialog, which) -> {
                updateOrderStatus(order, 3, position); // 鏀规垚 Delivered (3)
            });
        } else {
            // 宸茬稉瀹屾垚锛屽彧椤ず Close
            builder.setPositiveButton(R.string.close, null);
        }

        builder.setNegativeButton(R.string.cancel, null);

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
                            if (note.isEmpty() || note.equals("null")) note = context.getString(R.string.none);

                            StringBuilder msg = new StringBuilder();
                            msg.append(context.getString(R.string.customer_line, data.optString("customer", context.getString(R.string.guest)))).append("\n");
                            msg.append(context.getString(R.string.time_line, data.getString("time"))).append("\n\n");
                            msg.append(context.getString(R.string.items_header)).append("\n");

                            JSONArray items = data.getJSONArray("items");
                            for (int i=0; i<items.length(); i++) {
                                msg.append("- ").append(items.getString(i)).append("\n");
                            }

                            msg.append("\n").append(context.getString(R.string.note_header)).append("\n");
                            msg.append(note);

                            dialog.setMessage(msg.toString());
                        } else {
                            dialog.setMessage(context.getString(R.string.failed_load_details));
                        }
                    } catch (Exception e) {
                        dialog.setMessage(context.getString(R.string.error_prefix, e.getMessage()));
                    }
                },
                error -> dialog.setMessage(context.getString(R.string.error_network))
        );
        Volley.newRequestQueue(context).add(request);
    }

    private void updateOrderStatus(StaffOrder order, int nextStatus, int position) {
        StringRequest request = new StringRequest(Request.Method.POST, ApiConstants.updateOrderStatus(),
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if ("success".equalsIgnoreCase(json.optString("status"))) {
                            Toast.makeText(context, context.getString(R.string.order_updated), Toast.LENGTH_SHORT).show();
                            if (position >= 0 && position < orderList.size()) {
                                orderList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, orderList.size());
                            }
                        } else {
                            String msg = json.optString("message", context.getString(R.string.update_failed));
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, context.getString(R.string.invalid_server_response), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context, context.getString(R.string.error_updating), Toast.LENGTH_SHORT).show()) {
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
        Button btnViewDetails, btnPrimaryAction;
        View statusAccent;
        CardView orderCard;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tableNumber = itemView.findViewById(R.id.tableNumber);
            orderTime = itemView.findViewById(R.id.orderTime);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderSummary = itemView.findViewById(R.id.orderSummary);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnPrimaryAction = itemView.findViewById(R.id.btnPrimaryAction);
            statusAccent = itemView.findViewById(R.id.statusAccent);
            orderCard = itemView.findViewById(R.id.orderCard);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private String formatOrderTime(String rawDateTime) {
        if (rawDateTime == null || !rawDateTime.contains(" ")) return rawDateTime == null ? "" : rawDateTime;
        String[] parts = rawDateTime.split(" ");
        if (parts.length < 2) return rawDateTime;
        String[] hm = parts[1].split(":");
        if (hm.length < 2) return rawDateTime;
        return String.format(Locale.getDefault(), "%s:%s", hm[0], hm[1]);
    }
}

