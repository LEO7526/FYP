package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private final Context context;
    private final List<Map.Entry<CartItem, Integer>> items;

    public CartItemAdapter(Context context, Map<CartItem, Integer> cartItems) {
        this.context = context;
        this.items = new ArrayList<>();
        setHasStableIds(true);
        updateItems(cartItems);
    }

    /**
     * Aggregates quantities for identical CartItem keys and updates the list.
     */
    public void updateItems(Map<CartItem, Integer> newItems) {
        this.items.clear();
        if (newItems == null || newItems.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        LinkedHashMap<CartItem, Integer> aggregated = new LinkedHashMap<>();
        for (Map.Entry<CartItem, Integer> e : newItems.entrySet()) {
            CartItem key = e.getKey();
            int qty = e.getValue() == null ? 0 : e.getValue();
            if (key == null || qty <= 0) continue;
            int current = aggregated.getOrDefault(key, 0);
            aggregated.put(key, current + qty);
        }

        this.items.addAll(aggregated.entrySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<CartItem, Integer> entry = items.get(position);
        CartItem cartItem = entry.getKey();
        int quantity = entry.getValue();

        MenuItem item = cartItem.getMenuItem();

        holder.name.setText(item != null && item.getName() != null ? item.getName() : "Unnamed Dish");
        holder.description.setText(item != null && item.getDescription() != null ? item.getDescription() : "");

        // Show customization if present
        if (cartItem.getCustomization() != null) {
            StringBuilder details = new StringBuilder();
            String spice = cartItem.getCustomization().getSpiceLevel();
            String notes = cartItem.getCustomization().getExtraNotes();

            if (spice != null && !spice.isEmpty()) {
                details.append("Spice: ").append(spice);
            }
            if (notes != null && !notes.isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append(notes);
            }

            holder.customization.setText(details.toString());
            holder.customization.setVisibility(View.VISIBLE);
        } else {
            holder.customization.setVisibility(View.GONE);
        }

        holder.quantity.setText("×" + quantity);

        double price = 0.0;
        if (item != null) {
            try {
                price = item.getPrice();
            } catch (Exception ignored) {}
        }
        holder.price.setText(String.format(Locale.getDefault(), "$ %.2f", price * quantity));

        Glide.with(context)
                .load(item != null ? item.getImage_url() : null)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        Map.Entry<CartItem, Integer> entry = items.get(position);
        CartItem key = entry != null ? entry.getKey() : null;
        return key != null ? (long) key.hashCode() : super.getItemId(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, customization, description, price, quantity;
        ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cartItemName);
            customization = itemView.findViewById(R.id.cartDishCustomization);
            description = itemView.findViewById(R.id.cartItemDescription);
            price = itemView.findViewById(R.id.cartItemPrice);
            quantity = itemView.findViewById(R.id.cartItemQuantity);
            image = itemView.findViewById(R.id.cartItemImage);
        }
    }
}