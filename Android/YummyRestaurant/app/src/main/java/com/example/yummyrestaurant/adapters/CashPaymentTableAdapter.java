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
import com.example.yummyrestaurant.models.CashPaymentTable;

import java.util.List;

public class CashPaymentTableAdapter extends RecyclerView.Adapter<CashPaymentTableAdapter.TableViewHolder> {
    
    private Context context;
    private List<CashPaymentTable> tableList;
    private OnTableClickListener listener;
    
    // 顏色定義
    private static final int COLOR_CASH_PENDING = Color.parseColor("#FF9800"); // 橙色：待確認現金支付
    private static final int COLOR_BORDER = Color.parseColor("#FFFFFF");
    private static final int COLOR_TEXT = Color.WHITE;
    
    public interface OnTableClickListener {
        void onTableClicked(CashPaymentTable table);
    }
    
    public CashPaymentTableAdapter(Context context, List<CashPaymentTable> tableList, 
                                 OnTableClickListener listener) {
        this.context = context;
        this.tableList = tableList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
            .inflate(R.layout.item_cash_payment_table, parent, false);
        return new TableViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        CashPaymentTable table = tableList.get(position);
        holder.bind(table);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTableClicked(table);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return tableList.size();
    }
    
    class TableViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout tableContainer;
        private TextView textTableNumber;
        private TextView textCustomerName;
        private TextView textAmount;
        private TextView textTime;
        
        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tableContainer = itemView.findViewById(R.id.tableContainer);
            textTableNumber = itemView.findViewById(R.id.textTableNumber);
            textCustomerName = itemView.findViewById(R.id.textCustomerName);
            textAmount = itemView.findViewById(R.id.textAmount);
            textTime = itemView.findViewById(R.id.textTime);
        }
        
        public void bind(CashPaymentTable table) {
            // 設定桌號
            textTableNumber.setText(String.valueOf(table.getTableNumber()));
            
            // 設定客戶名稱
            String customerName = table.getCustomerName();
            if (customerName == null || customerName.isEmpty() || "null".equals(customerName)) {
                customerName = "匿名客戶";
            }
            textCustomerName.setText(customerName);
            
            // 設定金額
            textAmount.setText(String.format("$%.0f", table.getTotalAmount()));
            
            // 設定時間（只顯示時分）
            String orderTime = table.getOrderTime();
            if (orderTime != null && orderTime.contains(" ")) {
                String[] timeParts = orderTime.split(" ");
                if (timeParts.length > 1) {
                    String[] hourMinute = timeParts[1].split(":");
                    if (hourMinute.length >= 2) {
                        textTime.setText(String.format("%s:%s", hourMinute[0], hourMinute[1]));
                    } else {
                        textTime.setText(orderTime);
                    }
                } else {
                    textTime.setText(orderTime);
                }
            } else {
                textTime.setText(orderTime != null ? orderTime : "");
            }
            
            // 設定背景樣式
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(COLOR_CASH_PENDING);
            drawable.setStroke(4, COLOR_BORDER);
            drawable.setCornerRadius(12f);
            
            tableContainer.setBackground(drawable);
            
            // 設定文字顏色
            textTableNumber.setTextColor(COLOR_TEXT);
            textCustomerName.setTextColor(COLOR_TEXT);
            textAmount.setTextColor(COLOR_TEXT);
            textTime.setTextColor(COLOR_TEXT);
        }
    }
}