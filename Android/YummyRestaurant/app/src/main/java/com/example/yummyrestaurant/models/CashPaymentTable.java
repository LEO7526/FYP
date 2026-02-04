package com.example.yummyrestaurant.models;

public class CashPaymentTable {
    private int tableNumber;
    private int orderId;
    private String customerName;
    private String orderTime;
    private double totalAmount;
    private String itemsSummary;
    
    public CashPaymentTable(int tableNumber, int orderId, String customerName, 
                          String orderTime, double totalAmount, String itemsSummary) {
        this.tableNumber = tableNumber;
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.itemsSummary = itemsSummary;
    }
    
    // Getters
    public int getTableNumber() {
        return tableNumber;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public String getOrderTime() {
        return orderTime;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public String getItemsSummary() {
        return itemsSummary;
    }
    
    // Setters
    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public void setItemsSummary(String itemsSummary) {
        this.itemsSummary = itemsSummary;
    }

    @Override
    public String toString() {
        return "CashPaymentTable{" +
                "tableNumber=" + tableNumber +
                ", orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", orderTime='" + orderTime + '\'' +
                ", totalAmount=" + totalAmount +
                ", itemsSummary='" + itemsSummary + '\'' +
                '}';
    }
}