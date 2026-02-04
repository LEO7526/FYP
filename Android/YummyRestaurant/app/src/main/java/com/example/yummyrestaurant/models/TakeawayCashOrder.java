package com.example.yummyrestaurant.models;

public class TakeawayCashOrder {
    private int orderId;
    private String orderRef;
    private String customerName;
    private String orderTime;
    private double totalAmount;
    private String itemsSummary;

    public TakeawayCashOrder(int orderId, String orderRef, String customerName, 
                           String orderTime, double totalAmount, String itemsSummary) {
        this.orderId = orderId;
        this.orderRef = orderRef;
        this.customerName = customerName;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.itemsSummary = itemsSummary;
    }

    // Getters
    public int getOrderId() { return orderId; }
    public String getOrderRef() { return orderRef; }
    public String getCustomerName() { return customerName; }
    public String getOrderTime() { return orderTime; }
    public double getTotalAmount() { return totalAmount; }
    public String getItemsSummary() { return itemsSummary; }

    // Setters
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setOrderRef(String orderRef) { this.orderRef = orderRef; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setItemsSummary(String itemsSummary) { this.itemsSummary = itemsSummary; }
}