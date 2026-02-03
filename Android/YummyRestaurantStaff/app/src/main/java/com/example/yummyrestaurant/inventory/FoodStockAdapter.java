package com.example.yummyrestaurant.inventory;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.example.yummyrestaurant.R;

public class FoodStockAdapter extends RecyclerView.Adapter<FoodStockAdapter.FoodStockViewHolder> {

    private List<FoodStock> foodStockList = new ArrayList<>();

    public FoodStockAdapter() {}

    @NonNull
    @Override
    public FoodStockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_food_stock, parent, false);
        return new FoodStockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodStockViewHolder holder, int position) {
        FoodStock currentFoodStock = foodStockList.get(position);
        holder.bind(currentFoodStock);
    }

    @Override
    public int getItemCount() {
        return foodStockList.size();
    }

    public void setFoodStockList(List<FoodStock> foodStockList) {
        this.foodStockList = foodStockList;
        notifyDataSetChanged();
    }

    static class FoodStockViewHolder extends RecyclerView.ViewHolder {
        private final TextView foodNameTextView;
        private final TextView quantityTextView;

        public FoodStockViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameTextView = itemView.findViewById(R.id.text_food_name);
            quantityTextView = itemView.findViewById(R.id.text_producible_quantity);
        }

        public void bind(final FoodStock foodStock) {
            foodNameTextView.setText(foodStock.itemName);

            String quantityText = String.format(Locale.getDefault(),
                    "Producible: %d / Min Target: %d",
                    foodStock.producibleQty,
                    foodStock.minProducibleQty);
            quantityTextView.setText(quantityText);

            // 因為這個列表現在只顯示「不足」的項，所以統一把顏色設為紅色/橘色警告色
            if (foodStock.producibleQty <= 0) {
                quantityTextView.setText("OUT OF STOCK! (Producible: 0)");
                quantityTextView.setTextColor(Color.parseColor("#B71C1C")); // 深紅
            } else {
                quantityTextView.setTextColor(Color.parseColor("#E65100")); // 橘色警告
            }
        }
    }
}