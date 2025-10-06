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

    private Object menuItemStableId() {
        if (menuItem == null) return null;
        try {
            try { return menuItem.getClass().getMethod("getId").invoke(menuItem); } catch (NoSuchMethodException ignored) {}
            try { return menuItem.getClass().getMethod("get_id").invoke(menuItem); } catch (NoSuchMethodException ignored) {}
            try { return menuItem.getClass().getMethod("getUuid").invoke(menuItem); } catch (NoSuchMethodException ignored) {}
        } catch (Exception ignored) {}
        try {
            String name = (String) menuItem.getClass().getMethod("getName").invoke(menuItem);
            double price = (double) menuItem.getClass().getMethod("getPrice").invoke(menuItem);
            return (name == null ? "" : name) + "|" + price;
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
        return "CartItem{menuId=" + menuItemStableId() + ", customization=" + customization + "}";
    }
}