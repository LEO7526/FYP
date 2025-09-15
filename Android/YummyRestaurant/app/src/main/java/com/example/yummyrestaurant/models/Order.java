package com.example.yummyrestaurant.models;

public class Order {
    private String orderId;
    private String items;
    private String date;
    private String total;

    public Order(String orderId, String items, String date, String total) {
        this.orderId = orderId;
        this.items = items;
        this.date = date;
        this.total = total;
    }

    public String getOrderId() { return orderId; }
    public String getItems() { return items; }
    public String getDate() { return date; }
    public String getTotal() { return total; }
}