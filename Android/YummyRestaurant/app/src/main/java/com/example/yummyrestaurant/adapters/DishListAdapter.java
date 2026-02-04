package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        void onItemClick(int id);
    }

    // 建構子加入 listener
    public DishListAdapter(Context context, List<JSONObject> dishes, OnItemClickListener listener) {
        this.context = context;
        this.dishes = dishes;
        this.listener = listener;
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
        int id = dish.optInt("id", 0);

        holder.tvName.setText(name);
        holder.tvPrice.setText("$" + price);
        holder.tvCategory.setText(category);

        // 設定點擊事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(id);
        });
    }

    @Override
    public int getItemCount() { return dishes.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvCategory;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDishName);
            tvPrice = itemView.findViewById(R.id.tvDishPrice);
            tvCategory = itemView.findViewById(R.id.tvDishCategory);
        }
    }
}