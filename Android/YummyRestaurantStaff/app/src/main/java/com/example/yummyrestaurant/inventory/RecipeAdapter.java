package com.example.yummyrestaurant.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.example.yummyrestaurant.R;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList = new ArrayList<>();
    private final OnCookClickListener listener;

    public interface OnCookClickListener {
        void onCookClick(Recipe recipe);
    }

    public RecipeAdapter(OnCookClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe currentRecipe = recipeList.get(position);
        holder.bind(currentRecipe, listener);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipeList = recipes;
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView capacityTextView;
        private final Button cookButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_recipe_name);
            capacityTextView = itemView.findViewById(R.id.text_recipe_capacity);
            cookButton = itemView.findViewById(R.id.btn_cook);
        }

        public void bind(final Recipe recipe, final OnCookClickListener listener) {
            nameTextView.setText(recipe.itemName);
            if (recipe.ingredientCount <= 0 || !recipe.hasRecipe) {
                capacityTextView.setText(R.string.recipe_not_configured_yet);
                cookButton.setText(R.string.no_recipe);
                cookButton.setEnabled(false);
                cookButton.setAlpha(0.6f);
            } else if (recipe.maxProducible <= 0) {
                capacityTextView.setText(itemView.getContext().getString(R.string.max_producible_now, 0));
                cookButton.setText(R.string.out_of_stock);
                cookButton.setEnabled(false);
                cookButton.setAlpha(0.6f);
            } else {
                capacityTextView.setText(itemView.getContext().getString(R.string.max_producible_now, recipe.maxProducible));
                cookButton.setText(R.string.produce);
                cookButton.setEnabled(true);
                cookButton.setAlpha(1f);
            }
            cookButton.setOnClickListener(v -> listener.onCookClick(recipe));
        }
    }
}