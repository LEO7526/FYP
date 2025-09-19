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
import com.example.yummyrestaurant.models.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private final Context context;
    private final List<Map.Entry<MenuItem, Integer>> items;

    public CartItemAdapter(Context context, Map<MenuItem, Integer> cartItems) {
        this.context = context;
        this.items = new ArrayList<>(cartItems.entrySet());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MenuItem item = items.get(position).getKey();
        int quantity = items.get(position).getValue();

        holder.name.setText(item.getName());
        holder.description.setText(item.getDescription());
        holder.price.setText(String.format(Locale.getDefault(), "$ %.2f", item.getPrice()));
        holder.quantity.setText("x" + quantity);

        Glide.with(context).load(item.getImage_url()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, price, quantity;
        ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cartItemName);
            description = itemView.findViewById(R.id.cartItemDescription);
            price = itemView.findViewById(R.id.cartItemPrice);
            quantity = itemView.findViewById(R.id.cartItemQuantity);
            image = itemView.findViewById(R.id.cartItemImage);
        }
    }
}
