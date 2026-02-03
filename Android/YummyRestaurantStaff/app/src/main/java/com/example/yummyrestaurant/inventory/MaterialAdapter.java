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

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {

    private List<Material> materialList = new ArrayList<>();
    private final OnMaterialClickListener listener; // 這裡需要一個監聽器變數

    // 這裡是關鍵：定義一個公開的內部介面
    public interface OnMaterialClickListener {
        void onMaterialClick(Material material);
    }

    // 建構函式需要傳入一個監聽器的實作
    public MaterialAdapter(OnMaterialClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        Material currentMaterial = materialList.get(position);
        holder.bind(currentMaterial, listener); // 將監聽器傳遞給 ViewHolder
    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    public void setMaterials(List<Material> materials) {
        this.materialList = materials;
        notifyDataSetChanged();
    }

    static class MaterialViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView quantityTextView;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_material_name);
            quantityTextView = itemView.findViewById(R.id.text_material_quantity);
        }

        // bind 方法需要接收監聽器
        public void bind(final Material material, final OnMaterialClickListener listener) {
            nameTextView.setText(material.mname);
            // 修改這裡：使用 .unit 和 .reorderLevel
            String quantityText = String.format(Locale.getDefault(),
                    "Current: %.2f %s (Reorder at: %.2f %s)",
                    material.mqty, material.unit, material.reorderLevel, material.unit);
            quantityTextView.setText(quantityText);

            // 判斷是否低於安全水位
            if (material.mqty <= material.reorderLevel) {
                quantityTextView.setTextColor(Color.RED);
            } else {
                quantityTextView.setTextColor(Color.GRAY);
            }

            itemView.setOnClickListener(v -> listener.onMaterialClick(material));
        }
    }
}