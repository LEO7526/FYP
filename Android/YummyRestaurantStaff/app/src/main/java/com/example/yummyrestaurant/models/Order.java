package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Order {
    private int oid;
    private String odate;
    private int ostatus;
    private String cname;
    private String staff_name;
    private int table_number;

    @SerializedName("original_total_amount")
    private Double originalTotalAmount;

    @SerializedName("discount_amount")
    private Double discountAmount;

    @SerializedName("total_amount")
    private Double totalAmount;

    @SerializedName("items")
    private List<OrderItem> items;

    @SerializedName("packages")
    private List<OrderPackage> packages;

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public List<OrderPackage> getPackages() {
        return packages;
    }

    public void setPackages(List<OrderPackage> packages) {
        this.packages = packages;
    }


    public int getOid() {
        return oid;
    }

    public void setOid(int oid) {
        this.oid = oid;
    }

    public String getOdate() {
        return odate;
    }

    public void setOdate(String odate) {
        this.odate = odate;
    }


    public int getOstatus() {
        return ostatus;
    }

    public void setOstatus(int ostatus) {
        this.ostatus = ostatus;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getStaff_name() {
        return staff_name;
    }

    public void setStaff_name(String staff_name) {
        this.staff_name = staff_name;
    }

    public int getTable_number() {
        return table_number;
    }

    public void setTable_number(int table_number) {
        this.table_number = table_number;
    }

    public Double getOriginalTotalAmount() {
        return originalTotalAmount;
    }

    public void setOriginalTotalAmount(Double originalTotalAmount) {
        this.originalTotalAmount = originalTotalAmount;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    // Legacy compatibility accessors.
    public int getStatus() {
        return ostatus;
    }

    public String getTableNumber() {
        return table_number > 0 ? String.valueOf(table_number) : "-";
    }

    public String getType() {
        return table_number > 0 ? "dine_in" : "takeaway";
    }

    public String getSummary() {
        int itemCount = items == null ? 0 : items.size();
        return itemCount + " item(s)";
    }
}