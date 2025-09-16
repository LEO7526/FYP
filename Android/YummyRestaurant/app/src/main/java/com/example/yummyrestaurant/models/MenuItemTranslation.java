package com.example.yummyrestaurant.models;

public class MenuItemTranslation {
    private int item_id;
    private String item_name;
    private String item_description;
    private double item_price;
    private String image_url;
    private String spice_level;
    private String tags;

    // Getters and setters
    public int getItemId() { return item_id; }
    public String getItemName() { return item_name; }
    public String getItemDescription() { return item_description; }
    public double getItemPrice() { return item_price; }
    public String getImageUrl() { return image_url; }
    public String getSpiceLevel() { return spice_level; }
    public String getTags() { return tags; }
}