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

    // simplified filter: only by category
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
        holder.dishName.setText(item.getName() != null ? item.getName() : "");
        holder.dishDescription.setText(item.getDescription() != null ? item.getDescription() : "");
        holder.dishPrice.setText(String.format("$ %.2f", item.getPrice()));

        // Spice level with chili icons
        holder.spiceIconContainer.removeAllViews();

        int spiceCount = 0;
        try {
            spiceCount = Math.max(0, Math.min(4, item.getSpice_level()));
        } catch (Exception e) {
            spiceCount = 0;
        }

        if (spiceCount > 0) {
            for (int i = 0; i < spiceCount; i++) {
                ImageView chili = new ImageView(context);
                chili.setImageResource(R.drawable.ic_chili);

                // Tint color depending on spice index
                switch (spiceCount) {
                    case 1:
                        chili.setColorFilter(ContextCompat.getColor(context, R.color.spice_mild));
                        break;
                    case 2:
                        chili.setColorFilter(ContextCompat.getColor(context, R.color.spice_medium));
                        break;
                    case 3:
                        chili.setColorFilter(ContextCompat.getColor(context, R.color.spice_hot));
                        break;
                    case 4:
                        chili.setColorFilter(ContextCompat.getColor(context, R.color.spice_numbing));
                        break;
                    default:
                        chili.setColorFilter(ContextCompat.getColor(context, R.color.spice_medium));
                        break;
                }

                int sizePx = dpToPx(16);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
                if (i > 0) params.setMarginStart(dpToPx(4));
                chili.setLayoutParams(params);
                holder.spiceIconContainer.addView(chili);
            }
        } else {
            TextView noSpice = new TextView(context);
            noSpice.setText("No spice");
            noSpice.setTextSize(10);
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

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
