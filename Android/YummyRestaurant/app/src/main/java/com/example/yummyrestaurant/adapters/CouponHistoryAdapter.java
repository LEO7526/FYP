package com.example.yummyrestaurant.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.CouponHistoryItem;

import java.util.List;

public class CouponHistoryAdapter extends RecyclerView.Adapter<CouponHistoryAdapter.ViewHolder> {
    private final List<CouponHistoryItem> items;

    public CouponHistoryAdapter(List<CouponHistoryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coupon_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CouponHistoryItem item = items.get(position);
        holder.tvAction.setText(item.getAction());
        holder.tvDelta.setText((item.getDelta() > 0 ? "+" : "") + item.getDelta());
        holder.tvResulting.setText("Balance: " + item.getResulting_points());
        holder.tvDate.setText(item.getCreated_at());
        holder.tvNote.setText(item.getNote() != null ? item.getNote() : "");

        // Color coding: green for earned, red for redeemed
        if (item.getDelta() > 0) {
            holder.tvDelta.setTextColor(Color.parseColor("#4CAF50")); // green
        } else {
            holder.tvDelta.setTextColor(Color.parseColor("#F44336")); // red
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAction, tvDelta, tvResulting, tvDate, tvNote;
        ViewHolder(View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvDelta = itemView.findViewById(R.id.tvDelta);
            tvResulting = itemView.findViewById(R.id.tvResulting);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvNote = itemView.findViewById(R.id.tvNote);
        }
    }
}