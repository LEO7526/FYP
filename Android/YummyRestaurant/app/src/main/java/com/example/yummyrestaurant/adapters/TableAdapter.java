package com.example.yummyrestaurant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.Table;

import java.util.List;
import java.util.Locale;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {

    private List<Table> tableList;
    private int selectedPosition = -1;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Table table);
    }

    public TableAdapter(List<Table> tableList, OnItemClickListener listener) {
        this.tableList = tableList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_table, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        Table table = tableList.get(position);
        holder.bind(table);


        holder.itemView.setSelected(selectedPosition == position);
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    public Table getSelectedTable() {
        if (selectedPosition != -1) {
            return tableList.get(selectedPosition);
        }
        return null;
    }

    class TableViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTableId;
        TextView textViewTableCapacity;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTableId = itemView.findViewById(R.id.textViewTableId);
            textViewTableCapacity = itemView.findViewById(R.id.textViewTableCapacity);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Update selection state
                        int previousPosition = selectedPosition;
                        selectedPosition = position;
                        
                        // Only notify if there was a previously selected item
                        if (previousPosition != -1) {
                            notifyItemChanged(previousPosition);
                        }
                        notifyItemChanged(selectedPosition);

                        listener.onItemClick(tableList.get(position));

                    }
                }
            });
        }

        void bind(Table table) {
            textViewTableId.setText(String.format(Locale.US, "Table No. %d", table.getTid()));
            textViewTableCapacity.setText(String.format(Locale.US, "Capacity: %d People", table.getCapacity()));
        }
    }
}
