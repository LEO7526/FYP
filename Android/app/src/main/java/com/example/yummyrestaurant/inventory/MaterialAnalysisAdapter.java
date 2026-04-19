package com.example.yummyrestaurant.inventory;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.inventory.MaterialAnalysisActivity.MaterialAnalysisItem;

import java.util.List;

public class MaterialAnalysisAdapter extends RecyclerView.Adapter<MaterialAnalysisAdapter.ViewHolder> {

    public interface OnRestockClickListener {
        void onRestockClick(MaterialAnalysisActivity.MaterialAnalysisItem item);
    }

    private final List<MaterialAnalysisItem> items;
    private final OnRestockClickListener listener;

    public MaterialAnalysisAdapter(List<MaterialAnalysisItem> items, OnRestockClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material_analysis, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaterialAnalysisItem item = items.get(position);

        holder.textMaterialName.setText(item.mname);
        holder.textCurrentQty.setText(holder.itemView.getContext().getString(R.string.current_qty_text, item.currentQty, item.unit));
        holder.textAvgUsage.setText(holder.itemView.getContext().getString(R.string.avg_weekly_usage_last7d, item.avgWeeklyUsage, item.unit));
        holder.textRestockPoint.setText(holder.itemView.getContext().getString(R.string.warning_line_120, item.restockPoint, item.unit));

        if ("NEEDS_RESTOCK".equals(item.status)) {
            holder.textStatus.setText(holder.itemView.getContext().getString(R.string.restock_plus_text, item.restockAmount, item.unit));
            holder.textStatus.setTextColor(Color.WHITE);
            holder.textStatus.getBackground().setTint(ContextCompat.getColor(holder.itemView.getContext(), R.color.red_dark));
            holder.btnRestockItem.setVisibility(android.view.View.VISIBLE);
            holder.btnRestockItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRestockClick(item);
                }
            });
        } else {
            holder.textStatus.setText(R.string.ok_upper);
            holder.textStatus.setTextColor(Color.WHITE);
            holder.textStatus.getBackground().setTint(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_dark));
            holder.btnRestockItem.setVisibility(android.view.View.GONE);
            holder.btnRestockItem.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMaterialName;
        private final TextView textCurrentQty;
        private final TextView textAvgUsage;
        private final TextView textRestockPoint;
        private final TextView textStatus;
        private final Button btnRestockItem;

        public ViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            textMaterialName = itemView.findViewById(R.id.textMaterialName);
            textCurrentQty = itemView.findViewById(R.id.textCurrentQty);
            textAvgUsage = itemView.findViewById(R.id.textAvgUsage);
            textRestockPoint = itemView.findViewById(R.id.textRestockPoint);
            textStatus = itemView.findViewById(R.id.textStatus);
            btnRestockItem = itemView.findViewById(R.id.btnRestockItem);
        }
    }
}
