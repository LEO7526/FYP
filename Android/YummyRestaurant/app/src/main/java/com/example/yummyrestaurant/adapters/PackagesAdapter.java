package com.example.yummyrestaurant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.SetMenu;

import java.util.List;

public class PackagesAdapter extends RecyclerView.Adapter<PackagesAdapter.ViewHolder> {

    public interface OnPackageClickListener {
        void onPackageClick(SetMenu setMenu);
    }

    private final List<SetMenu> packages;
    private final OnPackageClickListener listener;

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
        holder.name.setText(setMenu.getName());
        holder.itemView.setOnClickListener(v -> listener.onPackageClick(setMenu));
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.packageName);
        }
    }
}