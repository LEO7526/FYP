package com.example.yummyrestaurant.utils;

import com.example.yummyrestaurant.models.MenuItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static final Map<MenuItem, Integer> cartItems = new LinkedHashMap<>();

    public static void addItem(MenuItem item) {
        cartItems.put(item, cartItems.getOrDefault(item, 0) + 1);
    }

    public static Map<MenuItem, Integer> getCartItems() {
        return new LinkedHashMap<>(cartItems);
    }

    public static double getTotalCost() {
        double total = 0;
        for (Map.Entry<MenuItem, Integer> entry : cartItems.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }

    public static int getTotalAmountInCents() {
        return (int) Math.round(getTotalCost() * 100);
    }

    public static void clearCart() {
        cartItems.clear();
    }

    public static void removeItem(MenuItem item) {
        cartItems.remove(item);
    }

    public static void updateQuantity(MenuItem item, int quantity) {
        if (quantity <= 0) {
            cartItems.remove(item);
        } else {
            cartItems.put(item, quantity);
        }
    }
}
