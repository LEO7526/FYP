package com.example.yummyrestaurant.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String name;
    private double price;
    private String description;

    public Product(String name, double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public static List<Product> getSampleProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Pizza", 10.5, "Delicious cheese pizza"));
        products.add(new Product("Burger", 8.0, "Juicy beef burger"));
        products.add(new Product("Pasta", 9.5, "Spaghetti with meat sauce"));
        return products;
    }
}
