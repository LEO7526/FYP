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
        // Inflate the layout for a single history item
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coupon_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CouponHistoryItem item = items.get(position);

        // Bind data to UI
        holder.tvAction.setText(item.getAction());
        holder.tvCouponTitle.setText(item.getCouponTitle()); // show coupon name
        holder.tvResulting.setText("Balance: " + item.getResulting_points());
        holder.tvDate.setText(item.getCreated_at());

        // Handle delta display
        if (item.getDelta() == 0) {
            holder.tvDelta.setText(""); // hide for free coupons
        } else {
            String deltaText = (item.getDelta() > 0 ? "+" : "") + item.getDelta();
            holder.tvDelta.setText(deltaText);
            holder.tvDelta.setTextColor(item.getDelta() > 0
                    ? Color.parseColor("#4CAF50") // green for earned
                    : Color.parseColor("#F44336")); // red for redeemed
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder pattern for efficient view recycling.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAction, tvCouponTitle, tvDelta, tvResulting, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvCouponTitle = itemView.findViewById(R.id.tvCouponTitle);
            tvDelta = itemView.findViewById(R.id.tvDelta);
            tvResulting = itemView.findViewById(R.id.tvResulting);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}