package com.example.yummyrestaurant.utils;

import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;

import java.util.LinkedHashMap;
import java.util.Map;

public class CartManager {

    // Preserve insertion order so items appear in the order they were added
    private static final Map<CartItem, Integer> cartItems = new LinkedHashMap<>();

    /**
     * Add an item to the cart. If it already exists (same dish + same customization),
     * increase its quantity.
     */
    public static void addItem(CartItem item) {
        if (cartItems.containsKey(item)) {
            int currentQty = cartItems.get(item);
            cartItems.put(item, currentQty + 1);
        } else {
            cartItems.put(item, 1);
        }
    }

    /**
     * Get all items in the cart with their quantities.
     */
    public static Map<CartItem, Integer> getCartItems() {
        return new LinkedHashMap<>(cartItems);
    }

    /**
     * Get the total cost of all items in the cart.
     */
    public static double getTotalCost() {
        double total = 0;
        for (Map.Entry<CartItem, Integer> entry : cartItems.entrySet()) {
            MenuItem menuItem = entry.getKey().getMenuItem();
            total += menuItem.getPrice() * entry.getValue();
        }
        return total;
    }

    /**
     * Get the total cost in cents (useful for payment APIs).
     */
    public static int getTotalAmountInCents() {
        return (int) Math.round(getTotalCost() * 100);
    }

    /**
     * Update the quantity of a specific item.
     * If quantity <= 0, the item is removed.
     */
    public static void updateQuantity(CartItem item, int quantity) {
        if (quantity <= 0) {
            cartItems.remove(item);
        } else {
            cartItems.put(item, quantity);
        }
    }

    /**
     * Remove a specific item from the cart.
     */
    public static void removeItem(CartItem item) {
        cartItems.remove(item);
    }

    /**
     * Clear the cart completely.
     */
    public static void clearCart() {
        cartItems.clear();
    }

    /**
     * Get the quantity of a specific item in the cart.
     */
    public static int getItemQuantity(CartItem item) {
        return cartItems.getOrDefault(item, 0);
    }

    /**
     * Check if the cart is empty.
     */
    public static boolean isEmpty() {
        return cartItems.isEmpty();
    }

    /**
     * Get the total number of items (sum of all quantities).
     */
    public static int getTotalItems() {
        int total = 0;
        for (int qty : cartItems.values()) {
            total += qty;
        }
        return total;
    }
}