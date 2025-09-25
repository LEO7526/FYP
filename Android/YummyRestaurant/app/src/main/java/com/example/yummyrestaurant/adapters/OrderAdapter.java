package com.example.yummyrestaurant.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.Order;
import com.example.yummyrestaurant.models.OrderItem;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.orderId.setText("Order #" + order.getOid());
        holder.customer.setText("Customer: " + order.getCname());
        holder.table.setText("Table: " + order.getTable_number());
        holder.staff.setText("Staff: " + order.getStaff_name());
        holder.date.setText("Date: " + order.getOdate());

        double total = 0;
        List<OrderItem> items = order.getItems();
        Log.d("OrderAdapter", "Order ID: " + order.getOid());


        if (items != null) {
            for (OrderItem item : items) {
                Log.d("OrderAdapter", "Item: " + item.getName() + ", Qty: " + item.getQuantity() + ", Price: " + item.getItemPrice());
                total += item.getItemPrice() * item.getQuantity(); // ✅ This line was missing
            }
        } else {
            Log.d("OrderAdapter", "Items list is null");
        }

        Log.d("OrderAdapter", "Order ID: " + order.getOid() + ", Items: " + (items != null ? items.size() : "null"));

        holder.total.setText(String.format("Total: $%.2f", total));
        holder.status.setText("Status: " + convertStatus(order.getOstatus()));
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, customer, table, staff, date, total, status;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            customer = itemView.findViewById(R.id.customer);
            table = itemView.findViewById(R.id.table);
            staff = itemView.findViewById(R.id.staff);
            date = itemView.findViewById(R.id.date);
            total = itemView.findViewById(R.id.total);
            status = itemView.findViewById(R.id.status);
        }
    }


    /*
     * Order status codes and their meanings:
     *
     * 1 - Pending: Order received, not started — used by kitchen staff to track new incoming orders.
     * 2 - Preparing: Kitchen is actively cooking — used by chefs and staff to manage workflow.
     * 3 - Delivered: Food has been served — used by waitstaff to confirm completion of service.
     * 4 - Cancelled: Order was voided — used by staff or system to record cancellations.
     * 5 - Paid: Bill has been settled — used by cashier or POS system to confirm payment.
     *
     * These statuses help the restaurant coordinate order progress across kitchen, service, and billing.
     */

    private String convertStatus(int code) {
        switch (code) {
            case 1: return "Pending";
            case 2: return "Preparing";
            case 3: return "Delivered";
            case 4: return "Cancelled";
            case 5: return "Paid";
            default: return "Unknown";
        }
    }
}