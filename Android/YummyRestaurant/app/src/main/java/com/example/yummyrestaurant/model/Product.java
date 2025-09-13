package com.example.yummyrestaurant.model;

public class Product {
    private int pid;
    private String pname;
    private double pcost;

    // Getters and setters
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

    public double getPcost() {
        return pcost;
    }

    public void setPcost(double pcost) {
        this.pcost = pcost;
    }
}