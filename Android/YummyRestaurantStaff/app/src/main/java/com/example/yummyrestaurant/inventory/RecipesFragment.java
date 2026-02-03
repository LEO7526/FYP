package com.example.yummyrestaurant.inventory; // <-- 已修正包名

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.yummyrestaurant.R;

public class RecipesFragment extends Fragment implements RecipeAdapter.OnCookClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecipeAdapter recipeAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getClient().create(ApiService.class);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_recipes);
        recipeAdapter = new RecipeAdapter(this);
        recyclerView.setAdapter(recipeAdapter);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_recipes);
        swipeRefreshLayout.setOnRefreshListener(this::fetchRecipes);
        fetchRecipes();
    }

    private void fetchRecipes() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        apiService.getRecipes().enqueue(new Callback<ApiResponse<List<Recipe>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Recipe>>> call, @NonNull Response<ApiResponse<List<Recipe>>> response) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                ApiResponse<List<Recipe>> apiResponse = response.body();
                if (isAdded() && response.isSuccessful() && apiResponse != null && apiResponse.success) {
                    recipeAdapter.setRecipes(apiResponse.data);
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load recipes.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Recipe>>> call, @NonNull Throwable t) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onCookClick(Recipe recipe) {
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("e.g., 1");
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("produce: " + recipe.itemName)
                .setMessage("Enter the quantity to cook:")
                .setView(input)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String quantityStr = input.getText().toString();
                    if (quantityStr.isEmpty() || quantityStr.equals("0")) {
                        Toast.makeText(getContext(), "Please enter a valid quantity.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int quantity = Integer.parseInt(quantityStr);
                        performCook(recipe.itemId, quantity);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid number format.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performCook(int itemId, int quantity) {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        CookRequest request = new CookRequest(itemId, quantity);
        apiService.cookRecipe(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (isAdded()) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (response.isSuccessful() && apiResponse != null && apiResponse.success) {
                        Toast.makeText(getContext(), "Stock deducted successfully!", Toast.LENGTH_SHORT).show();
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Success")
                                .setMessage("Stock has been updated. You can check the new stock levels in the 'Materials' tab.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        String message = (apiResponse != null) ? apiResponse.message : "Operation failed.";
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}