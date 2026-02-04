package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.activities.TableSelectionActivity;
import com.example.yummyrestaurant.models.StaffTable;

import java.util.List;

public class StaffTableAdapter extends RecyclerView.Adapter<StaffTableAdapter.TableViewHolder> {

    private Context context;
    private List<StaffTable> tableList;

    public StaffTableAdapter(Context context, List<StaffTable> tableList) {
        this.context = context;
        this.tableList = tableList;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_table_grid, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        int currentPos = holder.getBindingAdapterPosition();
        if (currentPos == RecyclerView.NO_POSITION) return;

        StaffTable table = tableList.get(currentPos);

        holder.tableNumber.setText(String.valueOf(table.getId()));
        holder.tableStatus.setText(table.getStatusText());

        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setShape(GradientDrawable.OVAL);

        // 設定顏色
        if (table.getStatus() == 1) {
            // Available (綠色)
            bgShape.setColor(Color.parseColor("#4CAF50"));
            holder.tableStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else if (table.getStatus() == 2) {
            // Occupied (紅色)
            bgShape.setColor(Color.parseColor("#D32F2F"));
            holder.tableStatus.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            // Empty (灰色)
            bgShape.setColor(Color.parseColor("#E0E0E0"));
            holder.tableStatus.setTextColor(Color.parseColor("#757575"));
        }

        holder.tableContainer.setBackground(bgShape);

        // 點擊事件：查看詳情
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof TableSelectionActivity) {
                ((TableSelectionActivity) context).onTableClicked(table.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        TextView tableNumber, tableStatus;
        FrameLayout tableContainer;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tableNumber = itemView.findViewById(R.id.tableNumber);
            tableStatus = itemView.findViewById(R.id.tableStatus);
            tableContainer = itemView.findViewById(R.id.tableContainer);
        }
    }
}