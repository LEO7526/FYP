package com.example.yummyrestaurant.models;

public class CashPaymentTable {
    private int tableNumber;
    private int orderId;
    private String customerName;
    private String orderTime;
    private double totalAmount;
    private String itemsSummary;
    private boolean completed;
    private String statusLabel;
    
    public CashPaymentTable(int tableNumber, int orderId, String customerName, 
                          String orderTime, double totalAmount, String itemsSummary) {
        this(tableNumber, orderId, customerName, orderTime, totalAmount, itemsSummary, false, "Pending");
    }

    public CashPaymentTable(int tableNumber, int orderId, String customerName,
                          String orderTime, double totalAmount, String itemsSummary,
                          boolean completed, String statusLabel) {
        this.tableNumber = tableNumber;
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.itemsSummary = itemsSummary;
        this.completed = completed;
        this.statusLabel = statusLabel;
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

    public boolean isCompleted() {
        return completed;
    }

    public String getStatusLabel() {
        return statusLabel;
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

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
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
                ", completed=" + completed +
                ", statusLabel='" + statusLabel + '\'' +
                '}';
    }
}