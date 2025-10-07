package com.example.yummyrestaurant.models;

import java.util.List;

public class SetMenu {
    private String name;
    private String description;
    private double price;
    private List<MenuItem> items;

    public SetMenu(String name, String description, double price, List<MenuItem> items) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.items = items;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public List<MenuItem> getItems() { return items; }
}