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
        private final Button cookButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_recipe_name);
            cookButton = itemView.findViewById(R.id.btn_cook);
        }

        public void bind(final Recipe recipe, final OnCookClickListener listener) {
            nameTextView.setText(recipe.itemName);
            cookButton.setOnClickListener(v -> listener.onCookClick(recipe));
        }
    }
}