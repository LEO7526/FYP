package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.TableOrder;

import java.util.List;

public class TableGridAdapter extends BaseAdapter {
    private Context context;
    private List<TableOrder> tableOrders;

    public TableGridAdapter(Context context, List<TableOrder> tableOrders) {
        this.context = context;
        this.tableOrders = tableOrders;
    }

    @Override
    public int getCount() {
        return tableOrders.size();
    }

    @Override
    public Object getItem(int position) {
        return tableOrders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return tableOrders.get(position).getToid();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.table_item, parent, false);
        }

        TextView tableNumberText = convertView.findViewById(R.id.tableNumberText);
        TextView tableStatusText = convertView.findViewById(R.id.tableStatusText);

        TableOrder order = tableOrders.get(position);
        tableNumberText.setText("Table " + order.getTableNumber());
        tableStatusText.setText(order.getStatus());

        return convertView;
    }
}