package com.example.yummyrestaurant.models;

public class CashPaymentTable {
    private int tableNumber;
    private int orderId;
    private String customerName;
    private String orderTime;
    private double totalAmount;
    private String itemsSummary;

    public CashPaymentTable(int tableNumber, int orderId, String customerName, String orderTime, double totalAmount, String itemsSummary) {
        this.tableNumber = tableNumber;
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.itemsSummary = itemsSummary;
    }

    public int getTableNumber() { return tableNumber; }
    public int getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getOrderTime() { return orderTime; }
    public double getTotalAmount() { return totalAmount; }
    public String getItemsSummary() { return itemsSummary; }
}