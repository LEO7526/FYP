package com.example.yummyrestaurant.utils;

import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CartManager {

    private static final Map<CartItem, Integer> cartItems = new LinkedHashMap<>();
    private CartManager() {}

    private static final String TAG = "CartDebug";

    private static Object stableMenuItemId(Object menuItem) {
        if (menuItem == null) return null;
        try {
            try { return menuItem.getClass().getMethod("getId").invoke(menuItem); } catch (NoSuchMethodException ignored) {}
            try { return menuItem.getClass().getMethod("get_id").invoke(menuItem); } catch (NoSuchMethodException ignored) {}
            try { return menuItem.getClass().getMethod("getUuid").invoke(menuItem); } catch (NoSuchMethodException ignored) {}
        } catch (Exception ignored) {}
        try {
            String name = (String) menuItem.getClass().getMethod("getName").invoke(menuItem);
            double price = (double) menuItem.getClass().getMethod("getPrice").invoke(menuItem);
            return (name == null ? "" : name) + "|" + price;
        } catch (Exception ignored) {}
        return menuItem.hashCode();
    }

    public static synchronized void addItem(CartItem item, int qty) {
        if (item == null || qty <= 0) return;
        int current = cartItems.getOrDefault(item, 0);
        cartItems.put(item, current + qty);

        Object stableId = stableMenuItemId(item.getMenuItem());
        android.util.Log.d(TAG, "addItem: stableId=" + stableId + " customization=" + item.getCustomization() + " qtyAdded=" + qty + " newQty=" + cartItems.get(item));
        logCartSnapshot();
    }

    public static synchronized void addItem(CartItem item) {
        addItem(item, 1);
    }

    public static synchronized void updateQuantity(CartItem item, int quantity) {
        if (item == null) return;
        if (quantity <= 0) cartItems.remove(item);
        else cartItems.put(item, quantity);

        Object stableId = stableMenuItemId(item.getMenuItem());
        android.util.Log.d(TAG, "updateQuantity: stableId=" + stableId + " customization=" + item.getCustomization() + " setTo=" + quantity);
        logCartSnapshot();
    }

    private static void logCartSnapshot() {
        android.util.Log.d(TAG, "---- Cart Snapshot ----");
        for (Map.Entry<CartItem, Integer> e : cartItems.entrySet()) {
            CartItem ci = e.getKey();
            Integer q = e.getValue();
            Object stableId = stableMenuItemId(ci.getMenuItem());
            android.util.Log.d(TAG, "keyHash=" + ci.hashCode() +
                    " stableId=" + stableId +
                    " customization=" + ci.getCustomization() +
                    " qty=" + q);
        }
        android.util.Log.d(TAG, "-----------------------");
    }

    public static synchronized Map<CartItem, Integer> getCartItems() {
        android.util.Log.d(TAG, "getCartItems called, size=" + cartItems.size());
        logCartSnapshot();
        return Collections.unmodifiableMap(new LinkedHashMap<>(cartItems));
    }

    public static synchronized int getItemQuantity(CartItem item) {
        return cartItems.getOrDefault(item, 0);
    }

    public static synchronized void removeItem(CartItem item) {
        cartItems.remove(item);
    }

    public static synchronized void clearCart() {
        cartItems.clear();
    }

    public static synchronized double getTotalCost() {
        double total = 0.0;
        for (Map.Entry<CartItem, Integer> e : cartItems.entrySet()) {
            try {
                double price = e.getKey().getMenuItem().getPrice();
                total += price * e.getValue();
            } catch (Exception ignored) {}
        }
        return total;
    }

    public static synchronized int getTotalAmountInCents() {
        return (int) Math.round(getTotalCost() * 100);
    }

    public static synchronized boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public static synchronized int getTotalItems() {
        int total = 0;
        for (int qty : cartItems.values()) total += qty;
        return total;
    }
}