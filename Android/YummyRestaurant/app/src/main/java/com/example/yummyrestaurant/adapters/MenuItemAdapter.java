package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem item = filteredList.get(position);

        holder.nameTextView.setText(item.getName());
        holder.descriptionTextView.setText(item.getDescription());
        holder.priceTextView.setText(String.format("¥ %.2f", item.getPrice()));
        holder.categoryTextView.setText(item.getCategory());

        Glide.with(context)
                .load("http://10.0.2.2/NewFolder/Database/projectapi/" + item.getImage_url())
                .placeholder(R.drawable.placeholder)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView, descriptionTextView;
        TextView categoryTextView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.menuItemName);
            priceTextView = itemView.findViewById(R.id.menuItemPrice);
            descriptionTextView = itemView.findViewById(R.id.menuItemDescription);
            categoryTextView = itemView.findViewById(R.id.menuItemCategory);
            imageView = itemView.findViewById(R.id.menuItemImage);
        }
    }
}