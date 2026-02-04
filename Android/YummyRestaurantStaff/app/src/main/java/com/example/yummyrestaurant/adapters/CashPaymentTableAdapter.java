package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.CashPaymentTable;

import java.util.List;

public class CashPaymentTableAdapter extends RecyclerView.Adapter<CashPaymentTableAdapter.ViewHolder> {
    private Context context;
    private List<CashPaymentTable> tableList;
    private OnTableClickListener listener;

    public interface OnTableClickListener {
        void onTableClick(CashPaymentTable table);
    }

    public CashPaymentTableAdapter(Context context, List<CashPaymentTable> tableList, OnTableClickListener listener) {
        this.context = context;
        this.tableList = tableList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CashPaymentTable table = tableList.get(position);
        
        holder.text1.setText("Table " + table.getTableNumber() + " - " + table.getCustomerName());
        holder.text2.setText("HK$" + String.format("%.2f", table.getTotalAmount()));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTableClick(table);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}