package com.example.yummyrestaurant.utils;

import com.example.yummyrestaurant.models.MenuItem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class CartManager {

    // Use LinkedHashMap to preserve insertion order
    private static final Map<MenuItem, Integer> cartItems = new LinkedHashMap<>();

    /**
     * Add an item to the cart. If it already exists, increase its quantity.
     */
    public static void addItem(MenuItem item) {
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
    public static Map<MenuItem, Integer> getCartItems() {
        return new LinkedHashMap<>(cartItems);
    }

    /**
     * Get the total cost of all items in the cart.
     */
    public static double getTotalCost() {
        double total = 0;
        for (Map.Entry<MenuItem, Integer> entry : cartItems.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
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
     * Clear the cart completely.
     */
    public static void clearCart() {
        cartItems.clear();
    }

    /**
     * Remove a specific item from the cart.
     */
    public static void removeItem(MenuItem item) {
        cartItems.remove(item);
    }

    /**
     * Update the quantity of a specific item.
     * If quantity <= 0, the item is removed.
     */
    public static void updateQuantity(MenuItem item, int quantity) {
        if (quantity <= 0) {
            cartItems.remove(item);
        } else {
            cartItems.put(item, quantity);
        }
    }

    /**
     * Get the quantity of a specific item in the cart.
     */
    public static int getItemQuantity(MenuItem item) {
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