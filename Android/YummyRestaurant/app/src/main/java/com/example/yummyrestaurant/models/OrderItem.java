package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;

public class OrderItem {
    @SerializedName("pid")
    private int pid;

    @SerializedName("pname")
    private String pname;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("itemPrice")
    private double itemPrice;

    @SerializedName("itemCost")
    private double itemCost;

    public OrderItem(int pid, String pname, int quantity, double itemPrice, double itemCost) {
        this.pid = pid;
        this.pname = pname;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
        this.itemCost = itemCost;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public double getItemCost() {
        return itemCost;
    }

    public void setItemCost(double itemCost) {
        this.itemCost = itemCost;
    }

    // Optional: calculate subtotal dynamically
    public double getTotalCost() {
        return itemPrice * quantity;
    }
}