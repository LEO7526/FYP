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

public class CouponListAdapter extends RecyclerView.Adapter<CouponListAdapter.ViewHolder> {

    private Context context;
    private List<JSONObject> coupons;
    private OnItemClickListener listener; // 新增

    // 介面
    public interface OnItemClickListener {
        void onItemClick(int id);
    }

    public CouponListAdapter(Context context, List<JSONObject> coupons, OnItemClickListener listener) {
        this.context = context;
        this.coupons = coupons;
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
        JSONObject c = coupons.get(position);

        String title = c.optString("title", "No Title");
        String points = c.optString("points", "0") + " pts";
        String discount = c.optString("discount", "");
        String expiry = "Exp: " + c.optString("expiry", "-");
        int id = c.optInt("id", 0);

        holder.tvTitle.setText(title);
        holder.tvSub.setText(discount + " | " + expiry);
        holder.tvPoints.setText(points);

        // 點擊事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(id);
        });
    }

    @Override
    public int getItemCount() { return coupons.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSub, tvPoints;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvDishName);
            tvSub = itemView.findViewById(R.id.tvDishCategory);
            tvPoints = itemView.findViewById(R.id.tvDishPrice);
        }
    }
}