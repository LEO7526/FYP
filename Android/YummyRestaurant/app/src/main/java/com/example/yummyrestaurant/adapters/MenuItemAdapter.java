package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.activities.DishDetailActivity;
import com.example.yummyrestaurant.models.MenuItem;

import java.util.ArrayList;
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
        filter("All Dishes"); // default to show all
    }

    // ✅ simplified filter: only by category
    public void filter(String category) {
        Log.d("FilterStart", "Full list size: " + fullList.size());
        filteredList.clear();

        String selectedCategory = category != null ? category.trim().toLowerCase() : "all";
        if (selectedCategory.equals("all dishes")) {
            selectedCategory = "all";
        }

        for (MenuItem item : fullList) {
            String itemCategory = item.getCategory() != null
                    ? item.getCategory().trim().toLowerCase()
                    : "";

            boolean matchCategory = selectedCategory.equals("all")
                    || itemCategory.equals(selectedCategory);

            if (matchCategory) {
                filteredList.add(item);
            }
        }

        Log.d("FilterResult", "Filtered list size: " + filteredList.size());
        notifyDataSetChanged();
    }

    public void search(String query) {
        filteredList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (MenuItem item : fullList) {
                String name = item.getName() != null ? item.getName().toLowerCase() : "";
                if (name.contains(lowerQuery)) {
                    filteredList.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void searchByDishName(String query) {
        filteredList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (MenuItem item : fullList) {
                if (item.getName() != null && item.getName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(item);
                }
            }
        }

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
        holder.dishPrice.setText(String.format("$ %.2f", item.getPrice()));

        // Spice level with chili icons 🌶️
        holder.spiceIconContainer.removeAllViews();

        String spice = item.getSpice_level() != null ? item.getSpice_level().toLowerCase() : "";
        int spiceCount;

        switch (spice) {
            case "mild":
                spiceCount = 1;
                break;
            case "medium":
                spiceCount = 2;
                break;
            case "hot":
                spiceCount = 3;
                break;
            case "numbing":
                spiceCount = 4;
                break;
            default:
                spiceCount = 0;
                break;
        }

        if (spiceCount > 0) {
            for (int i = 0; i < spiceCount; i++) {
                ImageView chili = new ImageView(context);
                chili.setImageResource(R.drawable.ic_chili);

                // Tint color depending on spice level
                switch (spice) {
                    case "mild":
                        chili.setColorFilter(ContextCompat.getColor(context, R.color.spice_mild));
                        break;
                    case "medium":
                        chili.setColorFilter(ContextCompat.getColor(context, R.color.spice_medium));
                        break;
                    case "hot":
                        chili.setColorFilter(ContextCompat.getColor(context, R.color.spice_hot));
                        break;
                    case "numbing":
                        chili.setColorFilter(ContextCompat.getColor(context, R.color.spice_numbing));
                        break;
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        24, 24
                );
                if (i > 0) params.setMarginStart(4);
                chili.setLayoutParams(params);
                holder.spiceIconContainer.addView(chili);
            }
        } else {
            TextView noSpice = new TextView(context);
            noSpice.setText("No spice");
            noSpice.setTextSize(5);
            noSpice.setTextColor(Color.GRAY);
            holder.spiceIconContainer.addView(noSpice);
        }

        // Load dish image with Glide
        Glide.with(context)
                .load(item.getImage_url())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GlideError", "Failed to load image for: " + item.getName()
                                + " | URL: " + item.getImage_url()
                                + " | Error: " + (e != null ? e.getMessage() : "Unknown error"));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.d("GlideSuccess", "Image loaded for: " + item.getName());
                        return false;
                    }
                })
                .into(holder.dishImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DishDetailActivity.class);
            intent.putExtra("menuItem", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView dishImage;
        LinearLayout spiceIconContainer;
        TextView dishName, dishDescription, dishPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dishImage = itemView.findViewById(R.id.dishImage);
            dishName = itemView.findViewById(R.id.dishName);
            dishDescription = itemView.findViewById(R.id.dishDescription);
            dishPrice = itemView.findViewById(R.id.dishPrice);
            spiceIconContainer = itemView.findViewById(R.id.spiceIconContainer);
        }
    }
}