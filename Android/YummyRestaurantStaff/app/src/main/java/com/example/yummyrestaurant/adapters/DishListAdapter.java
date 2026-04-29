package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yummyrestaurant.R;
import org.json.JSONObject;
import java.util.List;

public class DishListAdapter extends RecyclerView.Adapter<DishListAdapter.ViewHolder> {

    private Context context;
    private List<JSONObject> dishes;
    private OnItemClickListener listener; // 新增監聽器

    // 定義介面
    public interface OnItemClickListener {
        void onItemClick(JSONObject item);
    }

    // 建構子加入 listener
    public DishListAdapter(Context context, List<JSONObject> dishes, OnItemClickListener listener) {
        this.context = context;
        this.dishes = dishes;
        this.listener = listener;
    }

    public void setData(List<JSONObject> newDishes) {
        this.dishes = newDishes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dish_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject dish = dishes.get(position);
        String name = dish.optString("name", "Unknown");
        String price = dish.optString("price", "0");
        String category = dish.optString("category", "-");
        String itemType = dish.optString("item_type", "dish").toLowerCase();

        holder.tvName.setText(name);
        holder.tvPrice.setText("$" + price);
        holder.tvCategory.setText(category);
        
        if (holder.tvType != null) {
            boolean isPackage = "package".equals(itemType);
            holder.tvType.setText(isPackage ? "🎁 PACKAGE" : "🍽️ DISH");

            // Apply appropriate background drawable based on type
            int badgeBackgroundRes = isPackage
                    ? R.drawable.bg_package_type_badge
                    : R.drawable.bg_dish_type_badge;
            holder.tvType.setBackground(ContextCompat.getDrawable(context, badgeBackgroundRes));
            holder.tvType.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }

        // 設定點擊事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(dish);
        });

        android.util.Log.d("DishListAdapter", "onBindViewHolder: name=" + name + ", price=" + price + ", category=" + category + ", type=" + itemType);
    }

    @Override
    public int getItemCount() { return dishes.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvCategory, tvType;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDishName);
            tvPrice = itemView.findViewById(R.id.tvDishPrice);
            tvCategory = itemView.findViewById(R.id.tvDishCategory);
            tvType = itemView.findViewById(R.id.tvDishType);
        }
    }
}