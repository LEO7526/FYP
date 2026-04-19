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

public class RecipesFragment extends Fragment implements RecipeAdapter.OnCookClickListener, RefreshableTab {

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
                    Toast.makeText(getContext(), R.string.failed_load_recipes, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Recipe>>> call, @NonNull Throwable t) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.network_error_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onCookClick(Recipe recipe) {
        if (recipe.ingredientCount <= 0 || !recipe.hasRecipe) {
            Toast.makeText(getContext(), R.string.dish_no_recipe_configured, Toast.LENGTH_SHORT).show();
            return;
        }

        if (recipe.maxProducible <= 0) {
            Toast.makeText(getContext(), R.string.not_enough_stock_produce, Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.e_g_1);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.produce_item, recipe.itemName))
                .setMessage(getString(R.string.enter_quantity_to_produce_max, recipe.maxProducible))
                .setView(input)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String quantityStr = input.getText().toString();
                    if (quantityStr.isEmpty() || quantityStr.equals("0")) {
                        Toast.makeText(getContext(), R.string.please_enter_valid_quantity, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int quantity = Integer.parseInt(quantityStr);
                        if (quantity > recipe.maxProducible) {
                            Toast.makeText(getContext(), getString(R.string.max_producible_now, recipe.maxProducible), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        performCook(recipe.itemId, quantity);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), R.string.invalid_number_format, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
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
                        Toast.makeText(getContext(), R.string.stock_deducted_successfully, Toast.LENGTH_SHORT).show();
                        fetchRecipes();
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.success)
                            .setMessage(R.string.stock_updated_check_ingredients)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    } else {
                        String message = (apiResponse != null) ? apiResponse.message : getString(R.string.operation_failed);
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), getString(R.string.network_error_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchRecipes();
    }

    @Override
    public void refreshData() {
        fetchRecipes();
    }
}