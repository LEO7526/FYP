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
import com.example.yummyrestaurant.utils.AnimationUtils;

import java.util.List;
import java.util.Locale;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    public interface OnRedeemClickListener {
        void onRedeemClick(Coupon coupon);
        void onLoginRequired();
    }

    // 👉 New interface for opening detail page
    public interface OnCouponClickListener {
        void onCouponClick(Coupon coupon);
    }

    private final List<Coupon> couponList;
    private final OnRedeemClickListener redeemListener;
    private final OnCouponClickListener detailListener;
    private boolean isLoggedIn;
    private int currentPoints; // track user's current points

    public CouponAdapter(List<Coupon> couponList,
                         OnRedeemClickListener redeemListener,
                         OnCouponClickListener detailListener,
                         boolean isLoggedIn) {
        this.couponList = couponList;
        this.redeemListener = redeemListener;
        this.detailListener = detailListener;
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
        AnimationUtils.animateItemEntry(holder.itemView, position);

        holder.tvTitle.setText(coupon.getTitle());
        holder.tvDescription.setText(coupon.getDescription());
        holder.tvPoints.setText(holder.itemView.getContext().getString(R.string.coupon_requires_points_format, coupon.getPointsRequired()));
        holder.tvExpiry.setText(
                coupon.getExpiryDate() != null && !coupon.getExpiryDate().isEmpty()
                ? holder.itemView.getContext().getString(R.string.coupon_valid_until_format, coupon.getExpiryDate())
                : holder.itemView.getContext().getString(R.string.coupon_no_expiry)
        );

        // 👉 Handle item click for details
        holder.itemView.setOnClickListener(v -> {
            if (detailListener != null) {
                detailListener.onCouponClick(coupon);
            }
        });

        // --- Redeem button logic ---
        if (!isLoggedIn) {
            holder.btnRedeem.setText(R.string.login_to_redeem);
            holder.btnRedeem.setEnabled(true);
            holder.btnRedeem.setAlpha(1f);
            holder.btnRedeem.setOnClickListener(v -> {
                if (redeemListener != null) redeemListener.onLoginRequired();
            });
            return;
        }

        // If coupon is not redeemable (e.g. already redeemed, or birthday not eligible)
        if (!coupon.isRedeemable()) {
            if (coupon.isBirthdayOnly()) {
                holder.btnRedeem.setText(R.string.birthday_not_eligible);
            } else {
                holder.btnRedeem.setText(R.string.already_redeemed);
            }
            holder.btnRedeem.setEnabled(false);
            holder.btnRedeem.setAlpha(0.5f);
            holder.btnRedeem.setOnClickListener(null);
            return;
        }

        // Check points requirement
        boolean hasEnoughPoints = currentPoints >= coupon.getPointsRequired();
        if (!hasEnoughPoints) {
            holder.btnRedeem.setText(R.string.not_enough_points);
            holder.btnRedeem.setEnabled(false);
            holder.btnRedeem.setAlpha(0.5f);
            holder.btnRedeem.setOnClickListener(null);
            return;
        }

        // ✅ Eligible to redeem
    holder.btnRedeem.setText(R.string.redeem);
        holder.btnRedeem.setEnabled(true);
        holder.btnRedeem.setAlpha(1f);
        holder.btnRedeem.setOnClickListener(v -> {
            if (redeemListener != null) {
                redeemListener.onRedeemClick(coupon);
            }
        });
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
