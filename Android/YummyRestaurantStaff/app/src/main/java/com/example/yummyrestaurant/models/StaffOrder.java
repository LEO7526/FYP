package com.example.yummyrestaurant.models;

public class StaffOrder {
    private int oid;
    private String tableNumber; // 改成 String 因為可能是 "Takeaway"
    private String orderTime;
    private int status;
    private String summary;
    private String type; // 新增: dine_in / takeaway

    public StaffOrder(int oid, String tableNumber, String orderTime, int status, String summary, String type) {
        this.oid = oid;
        this.tableNumber = tableNumber;
        this.orderTime = orderTime;
        this.status = status;
        this.summary = summary;
        this.type = type;
    }

    // Default constructor for JSON parsing
    public StaffOrder() {}

    public int getOid() { return oid; }
    public String getTableNumber() { return tableNumber; }
    public String getOrderTime() { return orderTime; }
    public int getStatus() { return status; }
    public String getSummary() { return summary; }
    public String getType() { return type; }
    
    public void setOid(int oid) { this.oid = oid; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }
    public void setStatus(int status) { this.status = status; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setType(String type) { this.type = type; }
}