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
}