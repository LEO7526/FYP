package com.example.fooddash;

public class CartItem {
    private final String name;
    private final double unitPrice;
    private int quantity;

    public CartItem(String name, double unitPrice, int quantity) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public void incrementQuantity() {
        quantity++;
    }

    public void decrementQuantity() {
        quantity--;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return unitPrice * quantity;
    }
}