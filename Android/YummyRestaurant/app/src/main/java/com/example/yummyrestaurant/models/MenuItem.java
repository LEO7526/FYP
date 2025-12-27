package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class MenuItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("price")
    private double price;

    @SerializedName("category_id") // <--- Add this annotation for proper GSON mapping
    private Integer categoryId;    // <--- Add this field (nullable to match backend if necessary)


    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("image_url")
    private String image_url;

    @SerializedName("spice_level")
    private int spice_level;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("category")
    private String category;

    // Transient field for storing customizations when used in packages
    // Not serialized to JSON by default
    private transient List<OrderItemCustomization> customizations;

    public MenuItem() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return id == menuItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    /** New helper: price in cents (int) */
    public int getPriceInCents() {
        return (int) Math.round(price * 100);
    }


    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getSpice_level() {
        return spice_level;
    }

    public void setSpice_level(int spice_level) {
        this.spice_level = spice_level;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public List<OrderItemCustomization> getCustomizations() {
        return customizations;
    }

    public void setCustomizations(List<OrderItemCustomization> customizations) {
        this.customizations = customizations;
    }

}
