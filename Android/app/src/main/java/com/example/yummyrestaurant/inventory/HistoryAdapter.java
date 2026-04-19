package com.example.yummyrestaurant.inventory;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.example.yummyrestaurant.R;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<ConsumptionLog> historyList = new ArrayList<>();
    private Context context;

    public HistoryAdapter() {}

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ConsumptionLog currentLog = historyList.get(position);
        holder.bind(currentLog, context);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void setHistoryLogs(List<ConsumptionLog> logs) {
        this.historyList = logs;
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateTextView;
        private final TextView typeTextView;
        private final TextView detailsTextView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // 确保这里的 ID 和 XML 中的 ID 完全匹配
            dateTextView = itemView.findViewById(R.id.text_history_date);
            typeTextView = itemView.findViewById(R.id.text_history_type);
            detailsTextView = itemView.findViewById(R.id.text_history_details);
        }

        public void bind(ConsumptionLog log, Context context) {
            dateTextView.setText(log.logDate);
            typeTextView.setText(log.logType.toUpperCase());
            detailsTextView.setText(log.details.trim());

            GradientDrawable background = (GradientDrawable) typeTextView.getBackground();
            int color;
            switch (log.logType.toLowerCase()) {
                case "reorder":
                    color = ContextCompat.getColor(context, android.R.color.holo_red_dark);
                    break;
                case "deduction":
                    color = ContextCompat.getColor(context, android.R.color.holo_orange_dark);
                    break;
                case "forecast":
                default:
                    color = ContextCompat.getColor(context, android.R.color.holo_blue_dark);
                    break;
            }
            background.setColor(color);
        }
    }
}