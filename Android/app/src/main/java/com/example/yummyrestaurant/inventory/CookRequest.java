package com.example.yummyrestaurant.inventory;

public class CookRequest {
    public int item_id;
    public int quantity_cooked;

    public CookRequest(int itemId, int quantity) {
        this.item_id = itemId;
        this.quantity_cooked = quantity;
    }
}