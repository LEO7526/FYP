package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.MaterialAvailabilityChecker;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    public interface CartUpdateListener {
        void onCartUpdated();
    }

    private final Context context;
    private final List<Map.Entry<CartItem, Integer>> items;
    private CartUpdateListener cartUpdateListener;

    public CartItemAdapter(Context context, Map<CartItem, Integer> cartItems) {
        this.context = context;
        this.items = new ArrayList<>();
        setHasStableIds(true);
        updateItems(cartItems);
    }

    public void setCartUpdateListener(CartUpdateListener listener) {
        this.cartUpdateListener = listener;
    }

    /**
     * Aggregates quantities for identical CartItem keys and updates the list.
     */
    public void updateItems(Map<CartItem, Integer> newItems) {
        this.items.clear();
        if (newItems == null || newItems.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        LinkedHashMap<CartItem, Integer> aggregated = new LinkedHashMap<>();
        for (Map.Entry<CartItem, Integer> e : newItems.entrySet()) {
            CartItem key = e.getKey();
            int qty = e.getValue() == null ? 0 : e.getValue();
            if (key == null || qty <= 0) continue;
            int current = aggregated.getOrDefault(key, 0);
            aggregated.put(key, current + qty);
        }

        this.items.addAll(aggregated.entrySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<CartItem, Integer> entry = items.get(position);
        CartItem cartItem = entry.getKey();
        int quantity = entry.getValue();

        MenuItem item = cartItem.getMenuItem();

        holder.name.setText(item != null && item.getName() != null ? item.getName() : "Unnamed Dish");
        holder.description.setText(item != null && item.getDescription() != null ? item.getDescription() : "");

        // Show customization if present
        if (cartItem.getCustomization() != null) {
            StringBuilder details = new StringBuilder();
            
            // ✅ v4.5支持：顯示所有customizationDetails，支援selectedValueIds和selectedChoices
            if (cartItem.getCustomization().getCustomizationDetails() != null && 
                !cartItem.getCustomization().getCustomizationDetails().isEmpty()) {
                
                for (com.example.yummyrestaurant.models.OrderItemCustomization detail : 
                     cartItem.getCustomization().getCustomizationDetails()) {
                    
                    // ✅ v4.5新增：優先使用selectedValues（值名稱）
                    List<String> displayValues = null;
                    if (detail.getSelectedValues() != null && !detail.getSelectedValues().isEmpty()) {
                        displayValues = detail.getSelectedValues();
                    } else if (detail.getSelectedChoices() != null && !detail.getSelectedChoices().isEmpty()) {
                        // ⚠️ 向後兼容：使用選擇名稱（v4.4）
                        displayValues = detail.getSelectedChoices();
                    }
                    
                    if (displayValues != null && !displayValues.isEmpty()) {
                        details.append("• ").append(detail.getOptionName() != null ? detail.getOptionName() : detail.getGroupName())
                               .append(": ")
                               .append(String.join(", ", displayValues));
                        
                        if (detail.getAdditionalCost() > 0) {
                            details.append(String.format(" (+₹%.2f)", detail.getAdditionalCost()));
                        }
                        details.append("\n");
                    }
                }
            }
            
            // 特殊要求
            String notes = cartItem.getCustomization().getExtraNotes();
            if (notes != null && !notes.isEmpty()) {
                details.append("• Notes: ").append(notes);
            }

            if (details.length() > 0) {
                holder.customization.setText(details.toString().trim());
                holder.customization.setVisibility(View.VISIBLE);
            } else {
                holder.customization.setVisibility(View.GONE);
            }
        } else {
            holder.customization.setVisibility(View.GONE);
        }

        holder.quantity.setText(String.valueOf(quantity));

        double price = 0.0;
        if (item != null) {
            try {
                price = item.getPrice();
            } catch (Exception ignored) {}
        }
        holder.price.setText(String.format(Locale.getDefault(), "$ %.2f", price * quantity));

        // Set up quantity controls
        holder.btnDecreaseQuantity.setOnClickListener(v -> {
            int currentQty = quantity;
            if (currentQty > 1) {
                CartManager.updateQuantity(cartItem, currentQty - 1);
                updateItems(CartManager.getCartItems());
                if (cartUpdateListener != null) {
                    cartUpdateListener.onCartUpdated();
                }
            } else {
                // If quantity is 1, decrease acts as delete
                CartManager.removeItem(cartItem);
                updateItems(CartManager.getCartItems());
                if (cartUpdateListener != null) {
                    cartUpdateListener.onCartUpdated();
                }
                Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnIncreaseQuantity.setOnClickListener(v -> {
            android.util.Log.d("MaterialCheck", "=== INCREASE QUANTITY CLICKED ===");
            android.util.Log.d("MaterialCheck", "Current item: " + (cartItem.getMenuItem() != null ? cartItem.getMenuItem().getId() : "null"));
            android.util.Log.d("MaterialCheck", "Current quantity: " + quantity);
            android.util.Log.d("MaterialCheck", "Attempting to add 1 more unit");
            
            // Check material availability before increasing quantity
            MaterialAvailabilityChecker.checkAdditionalQuantity(context, cartItem, 1, 
                new MaterialAvailabilityChecker.MaterialCheckCallback() {
                    @Override
                    public void onCheckComplete(boolean allAvailable, String message, JSONArray materialDetails) {
                        android.util.Log.d("MaterialCheck", "=== CHECK COMPLETE ===");
                        android.util.Log.d("MaterialCheck", "All available: " + allAvailable);
                        android.util.Log.d("MaterialCheck", "Message: " + message);
                        
                        if (allAvailable) {
                            // Materials are sufficient, increase quantity
                            android.util.Log.d("MaterialCheck", "✅ Materials sufficient - increasing quantity");
                            CartManager.updateQuantity(cartItem, quantity + 1);
                            updateItems(CartManager.getCartItems());
                            if (cartUpdateListener != null) {
                                cartUpdateListener.onCartUpdated();
                            }
                        } else {
                            // Insufficient materials, show detailed message
                            android.util.Log.d("MaterialCheck", "❌ Materials insufficient - blocking increase");
                            String detailedMessage = MaterialAvailabilityChecker.formatInsufficientMaterialsMessage(materialDetails);
                            Toast.makeText(context, 
                                "Cannot increase quantity:\n" + detailedMessage, 
                                Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCheckError(String error) {
                        android.util.Log.e("MaterialCheck", "=== CHECK ERROR ===");
                        android.util.Log.e("MaterialCheck", "Error: " + error);
                        
                        // On error, allow increasing quantity but warn user
                        CartManager.updateQuantity(cartItem, quantity + 1);
                        updateItems(CartManager.getCartItems());
                        if (cartUpdateListener != null) {
                            cartUpdateListener.onCartUpdated();
                        }
                        Toast.makeText(context, 
                            "Quantity increased (unable to verify ingredients: " + error + ")", 
                            Toast.LENGTH_SHORT).show();
                    }
                });
        });

        holder.btnDeleteItem.setOnClickListener(v -> {
            CartManager.removeItem(cartItem);
            updateItems(CartManager.getCartItems());
            if (cartUpdateListener != null) {
                cartUpdateListener.onCartUpdated();
            }
            Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
        });

        Glide.with(context)
                .load(item != null ? item.getImage_url() : null)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        Map.Entry<CartItem, Integer> entry = items.get(position);
        CartItem key = entry != null ? entry.getKey() : null;
        return key != null ? (long) key.hashCode() : super.getItemId(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, customization, description, price, quantity;
        ImageView image;
        ImageButton btnDecreaseQuantity, btnIncreaseQuantity, btnDeleteItem;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cartItemName);
            customization = itemView.findViewById(R.id.cartDishCustomization);
            description = itemView.findViewById(R.id.cartItemDescription);
            price = itemView.findViewById(R.id.cartItemPrice);
            quantity = itemView.findViewById(R.id.cartItemQuantity);
            image = itemView.findViewById(R.id.cartItemImage);
            btnDecreaseQuantity = itemView.findViewById(R.id.btnDecreaseQuantity);
            btnIncreaseQuantity = itemView.findViewById(R.id.btnIncreaseQuantity);
            btnDeleteItem = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}