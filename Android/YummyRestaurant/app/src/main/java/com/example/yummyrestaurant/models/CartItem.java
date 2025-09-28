package com.example.yummyrestaurant.models;

import java.util.Objects;

public class CartItem {
    private MenuItem menuItem;
    private Customization customization;

    public CartItem(MenuItem menuItem, Customization customization) {
        this.menuItem = menuItem;
        this.customization = customization;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public Customization getCustomization() { return customization; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem that = (CartItem) o;
        return Objects.equals(menuItem, that.menuItem) &&
                Objects.equals(customization, that.customization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuItem, customization);
    }
}