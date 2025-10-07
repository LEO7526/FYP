package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class SelectableMenuItemAdapter extends RecyclerView.Adapter<SelectableMenuItemAdapter.ViewHolder> {

    private final Context context;
    private final List<MenuItem> items;
    private final List<MenuItem> selectedItems = new ArrayList<>();
    private final int maxSelection; // how many items can be selected in this category

    public SelectableMenuItemAdapter(List<MenuItem> items, int maxSelection) {
        this.context = null; // not strictly needed, we use itemView.getContext()
        this.items = items != null ? items : new ArrayList<>();
        this.maxSelection = maxSelection;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selectable_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem item = items.get(position);

        // Set name
        holder.name.setText(item.getName() != null ? item.getName() : "Unnamed Dish");

        // Load image with Glide
        String imageUrl = item.getImage_url();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.placeholder);
        }

        // Highlight if selected
        if (selectedItems.contains(item)) {
            holder.itemView.setBackgroundResource(R.drawable.item_selected_background);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Handle click
        holder.itemView.setOnClickListener(v -> {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item);
                notifyItemChanged(position);
            } else {
                if (selectedItems.size() < maxSelection) {
                    selectedItems.add(item);
                    notifyItemChanged(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<MenuItem> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            image = itemView.findViewById(R.id.itemImage);
        }
    }
}