package com.example.yummyrestaurant.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.utils.CouponValidator;

import java.util.List;
import java.util.Locale;

public class MyCouponAdapter extends RecyclerView.Adapter<MyCouponAdapter.MyCouponViewHolder> {

    private static final String TAG = "MyCouponAdapter";

    public interface OnCouponClickListener {
        void onCouponSelected(Coupon coupon, int position);
    }

    private final List<Coupon> myCoupons;
    private final OnCouponClickListener listener;
    private final boolean fromCart;

    public MyCouponAdapter(List<Coupon> myCoupons, OnCouponClickListener listener, boolean fromCart) {
        this.myCoupons = myCoupons;
        this.listener = listener;
        this.fromCart = fromCart;
    }

    @NonNull
    @Override
    public MyCouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_coupon, parent, false);
        return new MyCouponViewHolder(view);
    }

    public void decrementCouponQuantity(int position, int quantity) {
        if (position >= 0 && position < myCoupons.size()) {
            Coupon coupon = myCoupons.get(position);
            int currentQty = coupon.getQuantity();

            int newQty = currentQty - quantity;
            if (newQty > 0) {
                coupon.setQuantity(newQty);
                notifyItemChanged(position);
            } else {
                myCoupons.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyCouponViewHolder holder, int position) {
        Coupon coupon = myCoupons.get(position);

        // Title with quantity
        if (coupon.getQuantity() > 1) {
            holder.tvTitle.setText(coupon.getTitle() + " x" + coupon.getQuantity());
        } else {
            holder.tvTitle.setText(coupon.getTitle());
        }

        // Description
        holder.tvDescription.setText(coupon.getDescription());

        // Toggle Discount vs Reward
        if ("free_item".equalsIgnoreCase(coupon.getType())) {
            holder.tvReward.setVisibility(View.VISIBLE);
            holder.tvDiscount.setVisibility(View.GONE);
            holder.tvReward.setText("Reward: Free " + coupon.getItemCategory());
        } else if ("percent".equalsIgnoreCase(coupon.getType())) {
            holder.tvReward.setVisibility(View.GONE);
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText("Discount: " + coupon.getDiscountAmount() + "% OFF");
        } else { // cash
            holder.tvReward.setVisibility(View.GONE);
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(
                    String.format(Locale.getDefault(), "Discount: HK$%.2f", coupon.getDiscountAmount() / 100.0)
            );
        }

        // Expiry
        if (coupon.getExpiryDate() != null && !coupon.getExpiryDate().isEmpty()) {
            holder.tvExpiry.setText("Valid until: " + coupon.getExpiryDate());
        } else {
            holder.tvExpiry.setText("No expiry");
        }

        // --- Validation ---
        boolean valid = fromCart && CouponValidator.isCouponValidForCart(coupon, 1);

        if (!valid) {
            holder.btnUse.setEnabled(false);
            holder.btnUse.setText("Not Applicable");
            holder.btnUse.setAlpha(0.5f);
            holder.itemView.setAlpha(0.7f);
        } else {
            holder.btnUse.setEnabled(true);
            holder.btnUse.setText("Use Coupon");
            holder.btnUse.setAlpha(1f);
            holder.itemView.setAlpha(1f);

            holder.btnUse.setOnClickListener(v -> {
                holder.btnUse.setEnabled(false);
                holder.btnUse.setText("Applying...");
                if (listener != null) {
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onCouponSelected(coupon, pos);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return myCoupons != null ? myCoupons.size() : 0;
    }

    static class MyCouponViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDiscount, tvReward, tvExpiry;
        Button btnUse;

        public MyCouponViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMyCouponTitle);
            tvDescription = itemView.findViewById(R.id.tvMyCouponDescription);
            tvDiscount = itemView.findViewById(R.id.tvMyCouponDiscount);
            tvExpiry = itemView.findViewById(R.id.tvMyCouponExpiry);
            btnUse = itemView.findViewById(R.id.btnUseCoupon);
            tvReward = itemView.findViewById(R.id.tvMyCouponReward);
        }
    }
}
