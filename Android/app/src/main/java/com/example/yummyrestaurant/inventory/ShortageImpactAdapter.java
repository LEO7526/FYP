package com.example.yummyrestaurant.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShortageImpactAdapter extends RecyclerView.Adapter<ShortageImpactAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onViewDetails(ShortageImpactItem item);
    }

    private final List<ShortageImpactItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public ShortageImpactAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ShortageImpactItem> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shortage_impact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShortageImpactItem item = items.get(position);
        holder.textName.setText(item.ingredientName);
        holder.textStock.setText(String.format(Locale.getDefault(),
                "Used this week: %.2f %s | Avg/day: %.2f %s",
                item.weeklyConsumed, item.unit, item.avgDailyConsumed, item.unit));
        holder.textImpact.setText(String.format(Locale.getDefault(),
                "Current stock: %.2f %s | Warning line: %.2f %s",
                item.currentQty, item.unit, item.reorderLevel, item.unit));

        String previewText;
        if (item.recentActivityCount > 0) {
            previewText = String.format(Locale.getDefault(),
                    "Recent activity: %d | Last: %s on %s\n%s",
                    item.recentActivityCount,
                    item.latestLogType,
                    item.latestLogDate,
                    item.latestLogDetails);
        } else {
            previewText = "No recent manual log. Weekly usage is calculated from completed orders.";
        }
        holder.textPreview.setText(previewText);
        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textStock, textImpact, textPreview;
        Button btnDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textImpactIngredientName);
            textStock = itemView.findViewById(R.id.textImpactStock);
            textImpact = itemView.findViewById(R.id.textImpactSummary);
            textPreview = itemView.findViewById(R.id.textImpactPreview);
            btnDetails = itemView.findViewById(R.id.btnViewAffectedDishes);
        }
    }
}