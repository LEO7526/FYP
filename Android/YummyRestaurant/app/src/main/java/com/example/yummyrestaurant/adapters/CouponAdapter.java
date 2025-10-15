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
        void onLoginRequired();
    }

    private final List<Coupon> couponList;
    private final OnRedeemClickListener listener;
    private boolean isLoggedIn;
    private int currentPoints; // track user's current points

    public CouponAdapter(List<Coupon> couponList, OnRedeemClickListener listener, boolean isLoggedIn) {
        this.couponList = couponList;
        this.listener = listener;
        this.isLoggedIn = isLoggedIn;
        this.currentPoints = 0;
    }

    // Call this when points are fetched
    public void setCurrentPoints(int points) {
        this.currentPoints = points;
        notifyDataSetChanged();
    }

    public void setLoggedIn(boolean loggedIn) {
        this.isLoggedIn = loggedIn;
        notifyDataSetChanged();
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
        holder.tvPoints.setText("Requires: " + coupon.getPoints_required() + " pts");
        holder.tvExpiry.setText(
                coupon.getExpiry_date() != null && !coupon.getExpiry_date().isEmpty()
                        ? "Valid until: " + coupon.getExpiry_date()
                        : "No expiry"
        );

        // Check if user can redeem
        boolean canRedeem = isLoggedIn && currentPoints >= coupon.getPoints_required();

        if (!isLoggedIn) {
            // Guest user
            holder.btnRedeem.setText("Login to Redeem");
            holder.btnRedeem.setEnabled(true);
            holder.itemView.setAlpha(1f); // show normally
            holder.btnRedeem.setOnClickListener(v -> {
                if (listener != null) listener.onLoginRequired();
            });
        } else if (canRedeem) {
            // Logged in and enough points
            holder.btnRedeem.setText("Redeem");
            holder.btnRedeem.setEnabled(true);
            holder.itemView.setAlpha(1f); // normal brightness
            holder.btnRedeem.setOnClickListener(v -> {
                if (listener != null) listener.onRedeemClick(coupon);
            });
        } else {
            // Logged in but not enough points
            holder.btnRedeem.setText("Not enough points");
            holder.btnRedeem.setEnabled(false);
            holder.itemView.setAlpha(0.5f); // dim the whole card
            holder.btnRedeem.setOnClickListener(null);
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