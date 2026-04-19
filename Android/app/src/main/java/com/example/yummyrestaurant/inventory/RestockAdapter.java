package com.example.yummyrestaurant.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RestockAdapter extends RecyclerView.Adapter<RestockAdapter.ViewHolder> {

    public interface OnDecisionListener {
        void onApprove(RestockRecommendation item);
        void onReject(RestockRecommendation item);
    }

    private final List<RestockRecommendation> items = new ArrayList<>();
    private final OnDecisionListener listener;

    public RestockAdapter(OnDecisionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<RestockRecommendation> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_restock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RestockRecommendation item = items.get(position);
        holder.tvName.setText(item.materialName + " (" + item.unit + ")");
        holder.tvMeta.setText(String.format(Locale.getDefault(),
                "Current: %.2f | Reorder Lv: %.2f | Avg/day: %.2f",
                item.currentQty, item.reorderLevel, item.avgDailyUsage));
        holder.tvProjected.setText(String.format(Locale.getDefault(),
                "Projected: %.2f %s", item.projectedUsage, item.unit));
        holder.tvWindow.setText(String.format(Locale.getDefault(), "Predict: %dd", item.periodDays));
        holder.tvSuggest.setText(String.format(Locale.getDefault(),
                "Suggested restock: %.0f %s", item.suggestedQty, item.unit));

        String status = item.status == null ? "pending" : item.status.toLowerCase(Locale.ROOT);
        holder.tvStatus.setText(status.toUpperCase(Locale.ROOT));
        int statusColor;
        switch (status) {
            case "approved":
                statusColor = R.color.green_dark;
                break;
            case "rejected":
                statusColor = R.color.red_dark;
                break;
            case "ordered":
                statusColor = R.color.blue_dark;
                break;
            default:
                statusColor = R.color.orange_dark;
                break;
        }
        holder.tvStatus.getBackground().setTint(ContextCompat.getColor(holder.itemView.getContext(), statusColor));

        boolean actionable = "pending".equals(status);
        holder.btnApprove.setVisibility(actionable ? View.VISIBLE : View.GONE);
        holder.btnReject.setVisibility(actionable ? View.VISIBLE : View.GONE);

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(item));
        holder.btnReject.setOnClickListener(v -> listener.onReject(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMeta, tvSuggest, tvStatus, tvProjected, tvWindow;
        Button btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRestockName);
            tvStatus = itemView.findViewById(R.id.tvRestockStatus);
            tvMeta = itemView.findViewById(R.id.tvRestockMeta);
            tvProjected = itemView.findViewById(R.id.tvRestockProjected);
            tvWindow = itemView.findViewById(R.id.tvRestockWindow);
            tvSuggest = itemView.findViewById(R.id.tvRestockSuggest);
            btnApprove = itemView.findViewById(R.id.btnApproveRestock);
            btnReject = itemView.findViewById(R.id.btnRejectRestock);
        }
    }
}
