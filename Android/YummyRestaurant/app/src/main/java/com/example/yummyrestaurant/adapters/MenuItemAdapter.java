package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.MenuItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {
    private Context context;
    private List<MenuItem> fullList;
    private List<MenuItem> filteredList;

    public MenuItemAdapter(Context context, List<MenuItem> menuItems) {
        this.context = context;
        this.fullList = new ArrayList<>(menuItems);
        this.filteredList = new ArrayList<>(menuItems);
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        fullList.clear();
        fullList.addAll(menuItems);
        filter("All", "All", "All");
    }

    public void filter(String category, String spice, String tag) {
        Log.d("FilterStart", "Full list size: " + fullList.size());
        filteredList.clear();
        for (MenuItem item : fullList) {
            String selectedCategory = category.trim().toLowerCase();
            String selectedSpice = spice.trim().toLowerCase();
            String selectedTag = tag.trim().toLowerCase();

            String itemCategory = item.getCategory() != null ? item.getCategory().trim().toLowerCase() : "";
            String itemSpice = item.getSpice_level() != null ? item.getSpice_level().trim().toLowerCase() : "";
            String itemTags = item.getTags() != null ? item.getTags().trim().toLowerCase() : "";

            boolean matchCategory = selectedCategory.equals("all") || itemCategory.equals(selectedCategory);
            boolean matchSpice = selectedSpice.equals("all") || itemSpice.equals(selectedSpice);
            boolean matchTag = selectedTag.equals("all") || Arrays.stream(itemTags.split(","))
                    .map(String::trim)
                    .anyMatch(t -> t.equals(selectedTag));

            Log.d("MatchCheck", "Item: " + item.getName() +
                    " | matchCategory: " + matchCategory +
                    " | matchSpice: " + matchSpice +
                    " | matchTag: " + matchTag);

            if (matchCategory && matchSpice && matchTag) {
                filteredList.add(item);
            }
        }

        Log.d("FilterResult", "Filtered list size: " + filteredList.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem item = filteredList.get(position);

        // Set text values
        holder.dishName.setText(item.getName());
        holder.dishDescription.setText(item.getDescription());
        holder.dishPrice.setText(String.format("¥ %.2f", item.getPrice()));
        holder.spiceLevel.setText("Spice: " + item.getSpice_level());

        // Load image with Glide
        Glide.with(context)
                .load(item.getImage_url())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GlideError", "Failed to load image for: " + item.getName() +
                                " | URL: " + item.getImage_url() +
                                " | Error: " + (e != null ? e.getMessage() : "Unknown error"));
                        return false; // Let Glide handle the error image
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.d("GlideSuccess", "Image loaded for: " + item.getName());
                        return false; // Let Glide handle displaying the image
                    }
                })
                .into(holder.dishImage);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView dishImage;
        TextView dishName, dishDescription, dishPrice, spiceLevel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dishImage = itemView.findViewById(R.id.dishImage);
            dishName = itemView.findViewById(R.id.dishName);
            dishDescription = itemView.findViewById(R.id.dishDescription);
            dishPrice = itemView.findViewById(R.id.dishPrice);
            spiceLevel = itemView.findViewById(R.id.spiceLevel);
        }
    }
}