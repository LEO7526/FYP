package com.example.yummyrestaurant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.SetMenu;
import com.example.yummyrestaurant.utils.ImageUrlResolver;
import com.example.yummyrestaurant.utils.PackageNameTranslator;

import java.util.List;

public class PackagesAdapter extends RecyclerView.Adapter<PackagesAdapter.ViewHolder> {

    public interface OnPackageClickListener {
        void onPackageClick(SetMenu setMenu);
    }

    private final List<SetMenu> packages;
    private final OnPackageClickListener listener;
    private String selectedPackageName = ""; // Track selected package

    public PackagesAdapter(List<SetMenu> packages, OnPackageClickListener listener) {
        this.packages = packages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SetMenu setMenu = packages.get(position);
        holder.name.setText(PackageNameTranslator.translate(holder.itemView.getContext(), setMenu.getName()));
        
        // Set background color based on selected package
        int bgColor = PackageNameTranslator.matches(holder.itemView.getContext(), setMenu.getName(), selectedPackageName) ? 
            holder.itemView.getContext().getResources().getColor(R.color.light_purple) :
            holder.itemView.getContext().getResources().getColor(R.color.light_gray);
        if (holder.imageContainer != null) {
            holder.imageContainer.setBackgroundColor(bgColor);
        }

        // Set card border based on selected package
        if (holder.cardView != null) {
            if (PackageNameTranslator.matches(holder.itemView.getContext(), setMenu.getName(), selectedPackageName)) {
                holder.cardView.setForeground(androidx.core.content.ContextCompat.getDrawable(
                    holder.itemView.getContext(), R.drawable.card_border_selected));
            } else {
                holder.cardView.setForeground(androidx.core.content.ContextCompat.getDrawable(
                    holder.itemView.getContext(), R.drawable.card_border_normal));
            }
        }
        
        // Load package image using Glide
        if (setMenu.getImageUrl() != null && !setMenu.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(ImageUrlResolver.resolve(setMenu.getImageUrl()))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.image);
        }
        
        holder.itemView.setOnClickListener(v -> listener.onPackageClick(setMenu));
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    // Get the position of the first package with a specific name
    public int getPositionForPackageName(String packageName) {
        String canonicalSearch = PackageNameTranslator.canonicalize(packageName);
        for (int i = 0; i < packages.size(); i++) {
            if (PackageNameTranslator.canonicalize(packages.get(i).getName()).equalsIgnoreCase(canonicalSearch)) {
                return i;
            }
        }
        return 0; // Return first position if not found
    }

    // Set the currently selected package for UI highlighting
    public void setSelectedPackageName(String packageName) {
        this.selectedPackageName = PackageNameTranslator.canonicalize(packageName);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;
        View imageContainer;
        androidx.cardview.widget.CardView cardView;
        ViewHolder(View itemView) {
            super(itemView);
            cardView = (androidx.cardview.widget.CardView) itemView;
            name = itemView.findViewById(R.id.packageName);
            image = itemView.findViewById(R.id.packageImage);
            imageContainer = itemView.findViewById(R.id.packageImageContainer);
        }
    }
}