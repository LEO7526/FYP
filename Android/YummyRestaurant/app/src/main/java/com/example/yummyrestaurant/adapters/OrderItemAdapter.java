package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.OrderItem;

import java.util.List;

public class OrderItemAdapter extends ArrayAdapter<OrderItem> {

    private final Context context;
    private final List<OrderItem> items;

    public OrderItemAdapter(@NonNull Context context, @NonNull List<OrderItem> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        OrderItem item = items.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        }

        TextView nameText = convertView.findViewById(R.id.itemNameText);
        TextView priceText = convertView.findViewById(R.id.itemPriceText);
        TextView quantityText = convertView.findViewById(R.id.itemQuantityText);
        TextView costText = convertView.findViewById(R.id.itemCostText);

        nameText.setText(item.getPname());
        priceText.setText("Unit: HK$" + String.format("%.2f", item.getItemPrice()));
        quantityText.setText("Qty: " + item.getQuantity());
        costText.setText("Subtotal: HK$" + String.format("%.2f", item.getItemCost()));

        return convertView;
    }
}