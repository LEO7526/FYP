package com.example.yummyrestaurant.models;

import java.io.Serializable;
import java.util.Objects;

public class CartItem implements Serializable {

    private final MenuItem menuItem;
    private final Customization customization;

    public CartItem(MenuItem menuItem, Customization customization) {
        this.menuItem = menuItem;
        this.customization = customization;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public Customization getCustomization() { return customization; }

    /** Delegate category to MenuItem */
    public String getCategory() {
        return menuItem != null ? menuItem.getCategory() : null;
    }

    /** --- ADDED: Delegate categoryId to MenuItem --- */
    public Integer getCategoryId() {
        return menuItem != null ? menuItem.getCategoryId() : null;
    }

    /** Base price in cents (delegates to MenuItem) */
    public int getPriceInCents() {
        return menuItem != null ? menuItem.getPriceInCents() : 0;
    }

    private Object menuItemStableId() {
        if (menuItem == null) return null;
        try {
            return menuItem.getId();
        } catch (Exception ignored) {}
        return menuItem.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem other = (CartItem) o;

        Object idA = menuItemStableId();
        Object idB = other.menuItemStableId();

        boolean sameMenu = (idA == null && idB == null) || (idA != null && idA.equals(idB));
        boolean sameCustomization = Objects.equals(this.customization, other.customization);

        return sameMenu && sameCustomization;
    }

    @Override
    public int hashCode() {
        Object id = menuItemStableId();
        int idHash = id != null ? id.hashCode() : 0;
        int customHash = customization != null ? customization.hashCode() : 0;
        return Objects.hash(idHash, customHash);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "menuItem=" + menuItem +
                ", customization=" + customization +
                '}';
    }
}