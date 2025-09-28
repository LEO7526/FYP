package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private final Context context;
    private final List<Map.Entry<CartItem, Integer>> items;

    public CartItemAdapter(Context context, Map<CartItem, Integer> cartItems) {
        this.context = context;
        this.items = new ArrayList<>(cartItems.entrySet());
    }

    public void updateItems(Map<CartItem, Integer> newItems) {
        this.items.clear();
        this.items.addAll(newItems.entrySet());
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Each entry is a Map.Entry<CartItem, Integer>
        Map.Entry<CartItem, Integer> entry = items.get(position);
        CartItem cartItem = entry.getKey();
        int quantity = entry.getValue();

        MenuItem item = cartItem.getMenuItem();

        // Dish name and description
        holder.name.setText(item.getName());
        holder.description.setText(item.getDescription());

        // Show customization if present
        if (cartItem.getCustomization() != null) {
            StringBuilder details = new StringBuilder();
            if (cartItem.getCustomization().getSpiceLevel() != null &&
                    !cartItem.getCustomization().getSpiceLevel().isEmpty()) {
                details.append("Spice: ").append(cartItem.getCustomization().getSpiceLevel());
            }
            if (cartItem.getCustomization().getExtraNotes() != null &&
                    !cartItem.getCustomization().getExtraNotes().isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append(cartItem.getCustomization().getExtraNotes());
            }
            holder.customization.setText(details.toString());
            holder.customization.setVisibility(View.VISIBLE);
        } else {
            holder.customization.setVisibility(View.GONE);
        }

        // Quantity and price
        holder.quantity.setText("x" + quantity);
        holder.price.setText(String.format(Locale.getDefault(),
                "$ %.2f", item.getPrice() * quantity));

        // Image
        Glide.with(context)
                .load(item.getImage_url())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
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