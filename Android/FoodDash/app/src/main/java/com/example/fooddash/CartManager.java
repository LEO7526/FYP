package com.example.fooddash;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class CartManager {

    private static final LinkedHashMap<String, CartItem> cartItems = new LinkedHashMap<>();
    private static double total = 0.0;

    public static void addItem(String name, double price) {
        if (cartItems.containsKey(name)) {
            cartItems.get(name).incrementQuantity();
        } else {
            cartItems.put(name, new CartItem(name, price, 1));
        }
        total += price;
    }

    public static void removeItem(String name) {
        if (cartItems.containsKey(name)) {
            CartItem item = cartItems.get(name);
            total -= item.getPrice();
            item.decrementQuantity();
            if (item.getQuantity() <= 0) {
                cartItems.remove(name);
            }
        }
    }

    public static List<String> getDisplayItems() {
        List<String> display = new ArrayList<>();
        for (CartItem item : cartItems.values()) {
            String line = item.getName() + " x" + item.getQuantity() + "    $" +
                    String.format(Locale.US, "%.2f", item.getTotalPrice());
            display.add(line);
        }
        return display;
    }

    public static double getTotal() {
        return total;
    }

    public static void clearCart() {
        cartItems.clear();
        total = 0.0;
    }
}