package com.example.yummyrestaurant.utils;

import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CartManager {

    private static final Map<CartItem, Integer> cartItems = new LinkedHashMap<>();

    // Track applied discounts (could be coupon IDs, codes, or types)
    private static final Map<String, Object> appliedDiscounts = new LinkedHashMap<>();

    // Track package details: packageId -> {items, price}
    private static final Map<Integer, Map<String, Object>> packageDetails = new HashMap<>();

    // Track prefill data for reorder: packageId -> list of items
    private static final Map<Integer, List<MenuItem>> prefillPackageData = new HashMap<>();

    // Track order type: dine_in or takeaway
    private static String orderType = null;
    
    // Track table number for dine_in orders
    private static Integer tableNumber = null;

    private CartManager() {
    }

    private static final String TAG = "CartDebug";

    private static Object stableMenuItemId(Object menuItem) {
        if (menuItem == null) return null;
        try {
            try {
                return menuItem.getClass().getMethod("getId").invoke(menuItem);
            } catch (NoSuchMethodException ignored) {
            }
            try {
                return menuItem.getClass().getMethod("get_id").invoke(menuItem);
            } catch (NoSuchMethodException ignored) {
            }
            try {
                return menuItem.getClass().getMethod("getUuid").invoke(menuItem);
            } catch (NoSuchMethodException ignored) {
            }
        } catch (Exception ignored) {
        }
        try {
            String name = (String) menuItem.getClass().getMethod("getName").invoke(menuItem);
            double price = (double) menuItem.getClass().getMethod("getPrice").invoke(menuItem);
            return (name == null ? "" : name) + "|" + price;
        } catch (Exception ignored) {
        }
        return menuItem.hashCode();
    }

    public static synchronized void addItem(CartItem item, int qty) {
        if (item == null || qty <= 0) return;
        int current = cartItems.getOrDefault(item, 0);
        cartItems.put(item, current + qty);

        Object stableId = stableMenuItemId(item.getMenuItem());
        android.util.Log.d(TAG, "addItem: stableId=" + stableId + " customization=" + item.getCustomization() + " qtyAdded=" + qty + " newQty=" + cartItems.get(item));
        logCartSnapshot();
    }

    public static synchronized void addItem(CartItem item) {
        addItem(item, 1);
    }

    public static synchronized void updateQuantity(CartItem item, int quantity) {
        if (item == null) return;
        if (quantity <= 0) cartItems.remove(item);
        else cartItems.put(item, quantity);

        Object stableId = stableMenuItemId(item.getMenuItem());
        android.util.Log.d(TAG, "updateQuantity: stableId=" + stableId + " customization=" + item.getCustomization() + " setTo=" + quantity);
        logCartSnapshot();
    }

    private static void logCartSnapshot() {
        android.util.Log.d(TAG, "---- Cart Snapshot ----");
        for (Map.Entry<CartItem, Integer> e : cartItems.entrySet()) {
            CartItem ci = e.getKey();
            Integer q = e.getValue();
            Object stableId = stableMenuItemId(ci.getMenuItem());
            android.util.Log.d(TAG, "keyHash=" + ci.hashCode() +
                    " stableId=" + stableId +
                    " customization=" + ci.getCustomization() +
                    " qty=" + q);
        }
        android.util.Log.d(TAG, "-----------------------");
    }

    public static synchronized Map<CartItem, Integer> getCartItems() {
        android.util.Log.d(TAG, "getCartItems called, size=" + cartItems.size());
        logCartSnapshot();
        return Collections.unmodifiableMap(new LinkedHashMap<>(cartItems));
    }

    public static synchronized int getItemQuantity(CartItem item) {
        return cartItems.getOrDefault(item, 0);
    }

    public static synchronized void removeItem(CartItem item) {
        cartItems.remove(item);
    }

    public static synchronized void clearCart() {
        cartItems.clear();
    }

    public static synchronized double getTotalCost() {
        double total = 0.0;
        for (Map.Entry<CartItem, Integer> e : cartItems.entrySet()) {
            try {
                double price = e.getKey().getMenuItem().getPrice();
                total += price * e.getValue();
            } catch (Exception ignored) {
            }
        }
        return total;
    }

    public static synchronized int getTotalAmountInCents() {
        return (int) Math.round(getTotalCost() * 100);
    }

    public static synchronized boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public static synchronized int getTotalItems() {
        int total = 0;
        for (int qty : cartItems.values()) total += qty;
        return total;
    }

    public static boolean hasItemCategory(String category) {
        if (cartItems == null || cartItems.isEmpty() || category == null) return false;

        for (CartItem cartItem : cartItems.keySet()) {
            MenuItem menuItem = cartItem.getMenuItem();
            if (menuItem != null && menuItem.getCategory() != null &&
                    menuItem.getCategory().equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }

    public static int getCheapestItemPrice(String category) {
        if (cartItems == null || cartItems.isEmpty()) return 0;

        int cheapest = Integer.MAX_VALUE;
        for (CartItem cartItem : cartItems.keySet()) {
            MenuItem menuItem = cartItem.getMenuItem();
            if (menuItem != null && menuItem.getCategory() != null &&
                    menuItem.getCategory().equalsIgnoreCase(category)) {
                int priceCents = menuItem.getPriceInCents();
                if (priceCents < cheapest) {
                    cheapest = priceCents;
                }
            }
        }
        return cheapest == Integer.MAX_VALUE ? 0 : cheapest;
    }

    public static boolean hasAnyItem(List<Integer> itemIds) {
        if (cartItems == null || cartItems.isEmpty() || itemIds == null) return false;

        for (CartItem cartItem : cartItems.keySet()) {
            MenuItem menuItem = cartItem.getMenuItem();
            if (menuItem != null && itemIds.contains(menuItem.getId())) {
                return true;
            }
        }
        return false;
    }

    public static int getCheapestEligibleItemPrice(Coupon coupon) {
        if (coupon == null) return 0;

        Map<CartItem, Integer> cartItems = getCartItems();
        int cheapest = Integer.MAX_VALUE;

        for (CartItem cartItem : cartItems.keySet()) {
            Integer itemId = cartItem.getMenuItemId();
            Integer categoryId = cartItem.getCategoryId();
            int price = cartItem.getPriceInCents();

            boolean eligible = false;

            // Check applicable items
            if (coupon.getApplicableItems() != null && !coupon.getApplicableItems().isEmpty()) {
                if (itemId != null && coupon.getApplicableItems().contains(itemId)) {
                    eligible = true;
                }
            }

            // Check applicable categories
            if (coupon.getApplicableCategories() != null && !coupon.getApplicableCategories().isEmpty()) {
                if (categoryId != null && coupon.getApplicableCategories().contains(categoryId)) {
                    eligible = true;
                }
            }

            if (eligible && price < cheapest) {
                cheapest = price;
            }
        }

        // If no eligible items found, return 0
        return (cheapest == Integer.MAX_VALUE) ? 0 : cheapest;
    }


    // Disabled until backend confirms category IDs
    public static boolean hasAnyCategory(List<Integer> categoryIds) {
        return false;
    }

    /**
     * Set the order type for this session
     * @param type One of: "dine_in", "takeaway", "delivery"
     */
    public static synchronized void setOrderType(String type) {
        if (type == null || (!type.equals("dine_in") && !type.equals("takeaway") && !type.equals("delivery"))) {
            android.util.Log.w(TAG, "setOrderType: Invalid order type: " + type);
            return;
        }
        orderType = type;
        android.util.Log.d(TAG, "setOrderType: " + type);
    }

    /**
     * Get the current order type
     * @return "dine_in", "takeaway", "delivery", or null if not set
     */
    public static synchronized String getOrderType() {
        return orderType;
    }

    /**
     * Check if an order type has been selected
     * @return true if order type is set
     */
    public static synchronized boolean isOrderTypeSelected() {
        return orderType != null;
    }

    /**
     * Set table number for dine_in orders
     * @param tableNum Table number (e.g., 5, 12)
     */
    public static synchronized void setTableNumber(Integer tableNum) {
        if (orderType != null && !orderType.equals("dine_in")) {
            android.util.Log.w(TAG, "setTableNumber: Only valid for dine_in orders");
            return;
        }
        tableNumber = tableNum;
        android.util.Log.d(TAG, "setTableNumber: " + tableNum);
    }

    /**
     * Get the table number for dine_in orders
     * @return Table number or null
     */
    public static synchronized Integer getTableNumber() {
        return tableNumber;
    }

    /**
     * Reset all order type related data for a new order session
     */
    public static synchronized void resetOrderTypeData() {
        orderType = null;
        tableNumber = null;
        android.util.Log.d(TAG, "resetOrderTypeData: All order type data cleared");
    }

    public static synchronized boolean hasOtherDiscountsApplied() {
        boolean result = !appliedDiscounts.isEmpty();
        android.util.Log.d(TAG, "hasOtherDiscountsApplied: " + result + " (count=" + appliedDiscounts.size() + ")");
        return result;
    }

    public static synchronized void applyDiscount(String discountKey, Object discountData) {
        if (discountKey == null) return;
        appliedDiscounts.put(discountKey, discountData);
        android.util.Log.d(TAG, "applyDiscount: key=" + discountKey + " data=" + discountData);
    }

    public static synchronized void removeDiscount(String discountKey) {
        if (discountKey == null) return;
        appliedDiscounts.remove(discountKey);
        android.util.Log.d(TAG, "removeDiscount: key=" + discountKey + " removed");
    }

    public static synchronized void clearDiscounts() {
        appliedDiscounts.clear();
        android.util.Log.d(TAG, "clearDiscounts: all discounts cleared");
    }

    // ✅ Package management
    public static synchronized void setPackageDetails(int packageId, List<MenuItem> items, double price) {
        Map<String, Object> details = new HashMap<>();
        details.put("items", items);
        details.put("price", price);
        packageDetails.put(packageId, details);
        android.util.Log.d(TAG, "setPackageDetails: packageId=" + packageId + ", itemCount=" + items.size() + ", price=" + price);
    }

    public static synchronized Map<Integer, Map<String, Object>> getPackageDetails() {
        return new HashMap<>(packageDetails);
    }

    public static synchronized void clearPackageDetails() {
        packageDetails.clear();
        android.util.Log.d(TAG, "clearPackageDetails: all package details cleared");
    }

    // ✅ Prefill data for package reorder
    public static synchronized void setPrefillPackageData(int packageId, List<MenuItem> items) {
        prefillPackageData.put(packageId, new ArrayList<>(items));
        android.util.Log.d(TAG, "setPrefillPackageData: packageId=" + packageId + ", itemCount=" + items.size());
    }

    public static synchronized List<MenuItem> getPrefillPackageData(int packageId) {
        return prefillPackageData.getOrDefault(packageId, new ArrayList<>());
    }

    public static synchronized void clearPrefillPackageData(int packageId) {
        prefillPackageData.remove(packageId);
        android.util.Log.d(TAG, "clearPrefillPackageData: packageId=" + packageId);
    }

    public static synchronized void clearAllPrefillData() {
        prefillPackageData.clear();
        android.util.Log.d(TAG, "clearAllPrefillData: all prefill data cleared");
    }
}