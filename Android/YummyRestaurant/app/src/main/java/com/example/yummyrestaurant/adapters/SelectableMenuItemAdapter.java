package com.example.yummyrestaurant.adapters;

import android.graphics.Color;
import android.util.Log;
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

    private final List<MenuItem> items;
    private final List<MenuItem> selectedItems = new ArrayList<>();
    private final int maxSelection; // how many items can be selected in this category

    public SelectableMenuItemAdapter(List<MenuItem> items, int maxSelection) {
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

        Log.d("SelectableAdapter", "Binding item: id=" + item.getId() + ", name=" + item.getName());

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
            holder.root.setBackgroundResource(R.drawable.item_selected_background);
        } else {
            holder.root.setBackgroundColor(Color.TRANSPARENT);
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

    /** Returns the currently selected items */
    public List<MenuItem> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    /** Returns how many items must/can be selected (used in validation) */
    public int getRequiredCount() {
        return maxSelection;
    }

    /** Preselect items for reorder (matches by name and price) */
    public void preselectItems(List<MenuItem> itemsToPreselect) {
        if (itemsToPreselect == null || itemsToPreselect.isEmpty()) {
            return;
        }

        selectedItems.clear();
        
        for (MenuItem prefillItem : itemsToPreselect) {
            for (MenuItem availableItem : items) {
                // Match by ID, or by name and price as fallback
                if ((prefillItem.getId() > 0 && prefillItem.getId() == availableItem.getId()) ||
                    (prefillItem.getName() != null && prefillItem.getName().equals(availableItem.getName()) &&
                     Math.abs(prefillItem.getPrice() - availableItem.getPrice()) < 0.01)) {
                    
                    if (!selectedItems.contains(availableItem) && selectedItems.size() < maxSelection) {
                        selectedItems.add(availableItem);
                        Log.d("SelectableAdapter", "Preselected item: " + availableItem.getName());
                    }
                    break;
                }
            }
        }
        
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;
        View root;

        ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.itemRoot);
            name = itemView.findViewById(R.id.itemName);
            image = itemView.findViewById(R.id.itemImage);
        }
    }
}