package com.example.yummyrestaurant.inventory;

public class StockAdjustRequest {

    public int material_id;
    public double quantity;
    public String action;

    public StockAdjustRequest(int material_id, double quantity, String action) {
        this.material_id = material_id;
        this.quantity = quantity;
        this.action = action;
    }
}