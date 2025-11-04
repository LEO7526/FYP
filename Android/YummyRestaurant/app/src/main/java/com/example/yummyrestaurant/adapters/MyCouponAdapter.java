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
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;

import java.util.List;
import java.util.Locale;

public class MyCouponAdapter extends RecyclerView.Adapter<MyCouponAdapter.MyCouponViewHolder> {

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
        boolean valid = fromCart && isCouponValidForCart(coupon);

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

    // --- Validation logic (mirrors CartActivity/MyCouponsActivity) ---
    private boolean isCouponValidForCart(Coupon coupon) {
        if (coupon == null) {
            Log.d("CouponDebug", "Coupon is null");
            return false;
        }

        Log.d("CouponDebug", "Validating coupon: " + coupon.getTitle() + " (ID=" + coupon.getCouponId() + ")");
        int totalCents = CartManager.getTotalAmountInCents();
        Log.d("CouponDebug", "Cart total (cents): " + totalCents);

        // 1. Minimum spend
        Double minSpend = coupon.getMinSpend();
        if (minSpend != null) {
            Log.d("CouponDebug", "Coupon minSpend=" + minSpend);
            if (totalCents < (int) Math.round(minSpend * 100)) {
                Log.d("CouponDebug", "Invalid: below min spend");
                return false;
            }
        }

        // 2. Applies to scope
        String appliesTo = coupon.getAppliesTo();
        Log.d("CouponDebug", "Coupon appliesTo=" + appliesTo);

        if ("item".equalsIgnoreCase(appliesTo)) {
            List<Integer> itemIds = coupon.getApplicableItems();
            if (itemIds != null && !itemIds.isEmpty()) {
                Log.d("CouponDebug", "Checking applicableItems=" + itemIds);
                if (!CartManager.hasAnyItem(itemIds)) {
                    Log.d("CouponDebug", "Invalid: no matching items in cart");
                    return false;
                }
            }

            String category = coupon.getItemCategory();
            if (category != null && !category.trim().isEmpty()) {
                Log.d("CouponDebug", "Checking itemCategory=" + category);
                if (!CartManager.hasItemCategory(category)) {
                    Log.d("CouponDebug", "Invalid: no matching category in cart");
                    return false;
                }
            }
        } else if ("category".equalsIgnoreCase(appliesTo)) {
            List<Integer> categoryIds = coupon.getApplicableCategories();
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Log.d("CouponDebug", "Checking applicableCategories=" + categoryIds);
                if (!CartManager.hasAnyCategory(categoryIds)) {
                    Log.d("CouponDebug", "Invalid: no matching categories in cart");
                    return false;
                }
            }
        }

        // 3. Order type
        String orderType = CartManager.getOrderType();
        Log.d("CouponDebug", "Order type=" + orderType);

        if ("dine_in".equals(orderType) && !coupon.isValidDineIn()) {
            Log.d("CouponDebug", "Invalid: not valid for dine-in");
            return false;
        }
        if ("takeaway".equals(orderType) && !coupon.isValidTakeaway()) {
            Log.d("CouponDebug", "Invalid: not valid for takeaway");
            return false;
        }
        if ("delivery".equals(orderType) && !coupon.isValidDelivery()) {
            Log.d("CouponDebug", "Invalid: not valid for delivery");
            return false;
        }

        // 4. Birthday-only
        if (coupon.isBirthdayOnly()) {
            Log.d("CouponDebug", "Coupon is birthday-only, checking RoleManager...");
            try {
                if (!RoleManager.isTodayUserBirthday()) {
                    Log.d("CouponDebug", "Invalid: not user's birthday");
                    return false;
                }
            } catch (Exception e) {
                Log.e("CouponDebug", "Error checking birthday", e);
                return false;
            }
        }

        // 5. Discount stacking
        if (!coupon.isCombineWithOtherDiscounts()) {
            Log.d("CouponDebug", "Coupon cannot combine with other discounts");
            if (CartManager.hasOtherDiscountsApplied()) {
                Log.d("CouponDebug", "Invalid: other discounts already applied");
                return false;
            }
        }

        Log.d("CouponDebug", "Coupon is valid ✅");
        return true;
    }
}