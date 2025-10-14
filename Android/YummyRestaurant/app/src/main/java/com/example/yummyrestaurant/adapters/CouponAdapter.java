package com.example.yummyrestaurant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.Coupon;

import java.util.List;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    public interface OnRedeemClickListener {
        void onRedeemClick(Coupon coupon);
    }

    private final List<Coupon> couponList;
    private final OnRedeemClickListener listener;
    private final boolean isLoggedIn;

    // Updated constructor: pass login state too
    public CouponAdapter(List<Coupon> couponList, OnRedeemClickListener listener, boolean isLoggedIn) {
        this.couponList = couponList;
        this.listener = listener;
        this.isLoggedIn = isLoggedIn;
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coupon_card, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Coupon coupon = couponList.get(position);

        holder.tvTitle.setText(coupon.getTitle());
        holder.tvDescription.setText(coupon.getDescription());
        holder.tvPoints.setText(String.format("Requires: %d pts", coupon.getPoints_required()));

        String expiry = coupon.getExpiry_date();
        if (expiry != null && !expiry.trim().isEmpty()) {
            holder.tvExpiry.setText("Valid until: " + expiry);
        } else {
            holder.tvExpiry.setText("No expiry");
        }

        // Configure Redeem button
        if (!isLoggedIn) {
            holder.btnRedeem.setEnabled(false);
            holder.btnRedeem.setText("Login to Redeem");
        } else {
            holder.btnRedeem.setEnabled(true);
            holder.btnRedeem.setText("Redeem");
            holder.btnRedeem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRedeemClick(coupon);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return couponList != null ? couponList.size() : 0;
    }

    public static class CouponViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvPoints, tvExpiry;
        Button btnRedeem;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCouponTitle);
            tvDescription = itemView.findViewById(R.id.tvCouponDescription);
            tvPoints = itemView.findViewById(R.id.tvCouponPointsRequired);
            tvExpiry = itemView.findViewById(R.id.tvCouponExpiry);
            btnRedeem = itemView.findViewById(R.id.btnRedeem);
        }
    }
}