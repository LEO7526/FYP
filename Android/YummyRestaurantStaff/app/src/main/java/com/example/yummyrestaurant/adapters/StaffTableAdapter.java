package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.activities.TableSelectionActivity;
import com.example.yummyrestaurant.models.StaffTable;

import java.util.List;
import java.util.Locale;

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
        if (table.getCapacity() > 0) {
            holder.tableCapacity.setText(context.getString(R.string.pax_format, table.getCapacity()));
            holder.tableCapacity.setVisibility(View.VISIBLE);
        } else {
            holder.tableCapacity.setVisibility(View.GONE);
        }
        String statusText = table.getStatusText() == null ? "" : table.getStatusText().trim().toLowerCase(Locale.ROOT);
        int status = table.getStatus();

        int circleColor;
        String badgeText;

        if (statusText.contains("occupied") || status == 2) {
            circleColor = Color.parseColor("#F44336");
            badgeText = context.getString(R.string.status_occupied);
        } else if (statusText.contains("book") || statusText.contains("reserv") || status == 1) {
            circleColor = Color.parseColor("#FF9800");
            badgeText = context.getString(R.string.status_reserved);
        } else {
            circleColor = Color.parseColor("#4CAF50");
            badgeText = context.getString(R.string.status_available);
        }

        holder.tableStatus.setText(badgeText);

        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setShape(GradientDrawable.OVAL);
        bgShape.setColor(circleColor);

        holder.tableContainer.setBackground(bgShape);

        GradientDrawable chip = (GradientDrawable) holder.tableStatus.getBackground().mutate();
        chip.setColor(circleColor);

        holder.tableIcon.setColorFilter(Color.WHITE);

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
        TextView tableNumber, tableStatus, tableCapacity;
        FrameLayout tableContainer;
        ImageView tableIcon;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tableNumber = itemView.findViewById(R.id.tableNumber);
            tableStatus = itemView.findViewById(R.id.tableStatus);
            tableCapacity = itemView.findViewById(R.id.tableCapacity);
            tableContainer = itemView.findViewById(R.id.tableContainer);
            tableIcon = itemView.findViewById(R.id.tableIcon);
        }
    }
}