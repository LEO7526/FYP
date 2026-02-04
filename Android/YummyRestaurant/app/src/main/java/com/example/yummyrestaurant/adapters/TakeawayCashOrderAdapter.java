package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.TakeawayCashOrder;

import java.util.List;

public class TakeawayCashOrderAdapter extends RecyclerView.Adapter<TakeawayCashOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<TakeawayCashOrder> orderList;
    private OnOrderClickListener clickListener;

    public interface OnOrderClickListener {
        void onOrderClick(TakeawayCashOrder order);
    }

    public TakeawayCashOrderAdapter(Context context, List<TakeawayCashOrder> orderList, OnOrderClickListener clickListener) {
        this.context = context;
        this.orderList = orderList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_takeaway_cash_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        TakeawayCashOrder order = orderList.get(position);

        holder.orderRef.setText(order.getOrderRef());
        holder.customerName.setText(order.getCustomerName());
        holder.orderTime.setText(order.getOrderTime());
        holder.totalAmount.setText(String.format("HK$%.2f", order.getTotalAmount()));
        holder.itemsSummary.setText(order.getItemsSummary());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderRef, customerName, orderTime, totalAmount, itemsSummary;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderRef = itemView.findViewById(R.id.textOrderRef);
            customerName = itemView.findViewById(R.id.textCustomerName);
            orderTime = itemView.findViewById(R.id.textOrderTime);
            totalAmount = itemView.findViewById(R.id.textTotalAmount);
            itemsSummary = itemView.findViewById(R.id.textItemsSummary);
        }
    }
}