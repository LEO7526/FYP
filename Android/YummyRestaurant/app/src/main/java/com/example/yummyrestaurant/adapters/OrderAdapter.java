package com.example.yummyrestaurant.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.activities.CartActivity;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Customization;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.models.Order;
import com.example.yummyrestaurant.models.OrderItem;
import com.example.yummyrestaurant.models.OrderItemCustomization;
import com.example.yummyrestaurant.models.OrderPackage;
import com.example.yummyrestaurant.models.OrderPackageDish;
import com.example.yummyrestaurant.utils.AnimationUtils;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.MaterialAvailabilityChecker;
import com.example.yummyrestaurant.utils.PackageNameTranslator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private static final int SPECIAL_OPTION_ID = 999; // Special requests/notes option ID
    private static final int MAX_PACKAGE_REORDER_QUANTITY = 99;
    private List<Order> orders;

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        AnimationUtils.animateItemEntry(holder.itemView, position);

        // 基本信息
        holder.orderId.setText("#" + order.getOid());
        
        double total = 0;
        List<OrderItem> items = buildDisplayItems(order);
        List<OrderPackage> packages = order.getPackages();
        
        Log.d("OrderAdapter", "=== BINDING ORDER #" + order.getOid() + " at position " + position + " ===");
        Log.d("OrderAdapter", "Packages: " + (packages != null ? packages.size() : "null"));
        Log.d("OrderAdapter", "Items: " + (items != null ? items.size() : "null"));

        holder.itemsContainer.removeAllViews();

        // 計數項目和計算總額
        int totalItemCount = 0;
        
        // 🔥 NEW: Create overview mode - limit display to 3 items max
        final int MAX_OVERVIEW_ITEMS = 3;
        int displayedItemCount = 0;
        
        // 顯示常規菜品（如果沒有套餐）
        if (items != null && (packages == null || packages.isEmpty())) {
            Log.d("OrderAdapter", "Processing " + items.size() + " regular items (no packages)");
            
            // Overview display: show limited items + "X more items" if needed
            for (int i = 0; i < items.size() && displayedItemCount < MAX_OVERVIEW_ITEMS; i++) {
                OrderItem item = items.get(i);
                totalItemCount += item.getQuantity();
                total += item.getItemPrice() * item.getQuantity();
                
                // Check if this is a package
                if (item.isPackage()) {
                    // Display package with expand icon
                    LinearLayout packageContainer = createPackageOverview(holder.itemsContainer.getContext(), item);
                    holder.itemsContainer.addView(packageContainer);
                } else {
                    // Regular item
                    TextView itemView = new TextView(holder.itemsContainer.getContext());
                    itemView.setText("• " + item.getName() + " x" + item.getQuantity());
                    itemView.setTextSize(12);
                    itemView.setPadding(8, 4, 8, 4);
                    itemView.setTextColor(Color.parseColor("#424242"));
                    holder.itemsContainer.addView(itemView);
                }
                
                displayedItemCount++;
            }
            
            // Count remaining items for total calculation
            for (int i = MAX_OVERVIEW_ITEMS; i < items.size(); i++) {
                OrderItem item = items.get(i);
                totalItemCount += item.getQuantity();
                total += item.getItemPrice() * item.getQuantity();
            }
            
            // Add "X more items" if there are hidden items
            if (items.size() > MAX_OVERVIEW_ITEMS) {
                TextView moreItemsView = new TextView(holder.itemsContainer.getContext());
                int hiddenCount = items.size() - MAX_OVERVIEW_ITEMS;
                moreItemsView.setText(holder.itemsContainer.getContext().getString(R.string.order_more_items_format, hiddenCount));
                moreItemsView.setTextSize(11);
                moreItemsView.setPadding(8, 4, 8, 4);
                moreItemsView.setTextColor(Color.parseColor("#888888"));
                moreItemsView.setTypeface(null, android.graphics.Typeface.ITALIC);
                
                // Click to expand
                moreItemsView.setOnClickListener(v -> {
                    // Remove overview and show all items
                    holder.itemsContainer.removeAllViews();
                    displayAllItems(holder, items, order);
                });
                
                holder.itemsContainer.addView(moreItemsView);
            }
        } else if (items != null) {
            // Process items (may include packages)
            Log.d("OrderAdapter", "Processing items with potential packages");
            
            for (int i = 0; i < items.size() && displayedItemCount < MAX_OVERVIEW_ITEMS; i++) {
                OrderItem item = items.get(i);
                totalItemCount += item.getQuantity();
                total += item.getItemPrice() * item.getQuantity();
                
                // Check if this is a package
                if (item.isPackage()) {
                    // Display package with expand icon
                    LinearLayout packageContainer = createPackageOverview(holder.itemsContainer.getContext(), item);
                    holder.itemsContainer.addView(packageContainer);
                } else {
                    // Regular item
                    TextView itemView = new TextView(holder.itemsContainer.getContext());
                    itemView.setText("• " + item.getName() + " x" + item.getQuantity());
                    itemView.setTextSize(12);
                    itemView.setPadding(8, 4, 8, 4);
                    itemView.setTextColor(Color.parseColor("#424242"));
                    holder.itemsContainer.addView(itemView);
                }
                
                displayedItemCount++;
            }
            
            // Count remaining items for total calculation
            for (int i = MAX_OVERVIEW_ITEMS; i < items.size(); i++) {
                OrderItem item = items.get(i);
                totalItemCount += item.getQuantity();
                total += item.getItemPrice() * item.getQuantity();
            }
            
            // Add "X more items" if there are hidden items
            if (items.size() > MAX_OVERVIEW_ITEMS) {
                TextView moreItemsView = new TextView(holder.itemsContainer.getContext());
                int hiddenCount = items.size() - MAX_OVERVIEW_ITEMS;
                moreItemsView.setText(holder.itemsContainer.getContext().getString(R.string.order_more_items_format, hiddenCount));
                moreItemsView.setTextSize(11);
                moreItemsView.setPadding(8, 4, 8, 4);
                moreItemsView.setTextColor(Color.parseColor("#888888"));
                moreItemsView.setTypeface(null, android.graphics.Typeface.ITALIC);
                
                // Click to expand
                moreItemsView.setOnClickListener(v -> {
                    // Remove overview and show all items
                    holder.itemsContainer.removeAllViews();
                    displayAllItems(holder, items, order);
                });
                
                holder.itemsContainer.addView(moreItemsView);
            }
        }

        // 設置項目計數
        holder.itemCount.setText(holder.itemView.getContext().getString(R.string.order_item_count_format, totalItemCount));
        Log.d("OrderAdapter", "Total item count: " + totalItemCount);
        
        // 設置總金額
        holder.totalAmount.setText(holder.itemView.getContext().getString(R.string.order_total_amount_format, total));
        Log.d("OrderAdapter", "Total amount: HK$" + String.format("%.2f", total));

        // 設置時間戳
        String timeAgoText = formatTimeAgo(holder.itemView.getContext(), order.getOdate());
        holder.timeAgo.setText(timeAgoText);
        Log.d("OrderAdapter", "Order date: " + order.getOdate() + " (" + timeAgoText + ")");

        // 設置狀態 Badge
        setStatusBadge(holder, order.getOstatus());
        Log.d("OrderAdapter", "Order status: " + order.getOstatus());

        // 設置狀態顏色條
        setStatusColorBar(holder, order.getOstatus());

        // 設置按鈕點擊事件
        holder.reorderBtn.setOnClickListener(v -> {
            Log.d("OrderAdapter", "🔄 Reorder clicked for order #" + order.getOid());
            handleReorder(holder.itemsContainer.getContext(), order);
        });

        holder.detailsBtn.setOnClickListener(v -> {
            Log.d("OrderAdapter", "ℹ️ Details clicked for order #" + order.getOid());
            Log.d("OrderAdapter", "Details clicked for order " + order.getOid());
            showOrderDetails(holder.itemsContainer.getContext(), order);
        });
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    private String formatTimeAgo(Context context, String orderDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(orderDate);
            long timeAgo = System.currentTimeMillis() - date.getTime();
            
            if (timeAgo < 60000) {
                return context.getString(R.string.time_just_now);
            } else if (timeAgo < 3600000) {
                long minutes = timeAgo / 60000;
                return context.getString(R.string.time_minutes_ago, minutes);
            } else if (timeAgo < 86400000) {
                long hours = timeAgo / 3600000;
                return context.getString(R.string.time_hours_ago, hours);
            } else {
                long days = timeAgo / 86400000;
                return context.getString(R.string.time_days_ago, days);
            }
        } catch (ParseException e) {
            return orderDate;
        }
    }

    private void setStatusBadge(OrderViewHolder holder, int status) {
        String statusText;
        int backgroundColor;
        
        switch (status) {
            case 0:
                statusText = holder.itemView.getContext().getString(R.string.status_awaiting_cash_payment);
                backgroundColor = Color.parseColor("#FF5722"); // 橘紅色 - 需要注意
                break;
            case 1:
                statusText = holder.itemView.getContext().getString(R.string.status_pending);
                backgroundColor = Color.parseColor("#FFC107"); // 黃色
                break;
            case 2:
                statusText = holder.itemView.getContext().getString(R.string.status_preparing);
                backgroundColor = Color.parseColor("#2196F3"); // 藍色
                break;
            case 3:
                statusText = holder.itemView.getContext().getString(R.string.status_delivered);
                backgroundColor = Color.parseColor("#4CAF50"); // 綠色
                break;
            case 4:
                statusText = holder.itemView.getContext().getString(R.string.cancelled);
                backgroundColor = Color.parseColor("#F44336"); // 紅色
                break;
            case 5:
                statusText = holder.itemView.getContext().getString(R.string.status_paid);
                backgroundColor = Color.parseColor("#4CAF50"); // 綠色
                break;
            default:
                statusText = holder.itemView.getContext().getString(R.string.status_unknown);
                backgroundColor = Color.parseColor("#9E9E9E"); // 灰色
        }
        
        holder.statusBadge.setText(statusText);
        holder.statusBadge.setBackgroundColor(backgroundColor);
    }

    private void setStatusColorBar(OrderViewHolder holder, int status) {
        int color;
        switch (status) {
            case 1:
                color = Color.parseColor("#FFC107"); // 黃色
                break;
            case 2:
                color = Color.parseColor("#2196F3"); // 藍色
                break;
            case 3:
            case 5:
                color = Color.parseColor("#4CAF50"); // 綠色
                break;
            case 4:
                color = Color.parseColor("#F44336"); // 紅色
                break;
            default:
                color = Color.parseColor("#9E9E9E"); // 灰色
        }
        holder.statusColorBar.setBackgroundColor(color);
    }

    private void handleReorder(Context context, Order order) {
        try {
            Log.d("OrderAdapter", "=== HANDLING REORDER FOR ORDER #" + order.getOid() + " ===");
            
            // 檢查訂單類型
            if (order.getPackages() != null && order.getPackages().size() > 0) {
                // ===== 套餐訂單：導航到 BuildSetMenuActivity 進行重新選擇 =====
                Log.d("OrderAdapter", "This is a PACKAGE order with " + order.getPackages().size() + " package(s)");
                OrderPackage pkg = order.getPackages().get(0);
                if (pkg == null || pkg.getPackageId() <= 0) {
                    Toast.makeText(context, context.getString(R.string.reorder_package_unavailable), Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("OrderAdapter", "Package: id=" + pkg.getPackageId() + 
                           ", name=" + pkg.getPackageName() + 
                           ", dishes=" + (pkg.getDishes() != null ? pkg.getDishes().size() : 0));
                
                // 存儲預填菜品到 CartManager
                List<MenuItem> prefilledItems = new ArrayList<>();
                if (pkg.getDishes() != null) {
                    for (OrderPackageDish dish : pkg.getDishes()) {
                        MenuItem item = new MenuItem();
                        item.setId(dish.getItemId());
                        item.setName(dish.getName());
                        item.setPrice(dish.getPrice());
                        prefilledItems.add(item);
                        Log.d("OrderAdapter", "  Prefill item: " + dish.getName() + " (id=" + dish.getItemId() + ")");
                    }
                }
                
                Log.d("OrderAdapter", "Storing " + prefilledItems.size() + " items in CartManager for prefill");
                CartManager.setPrefillPackageData(pkg.getPackageId(), prefilledItems);

                if (!CartManager.isOrderTypeSelected()) {
                    int fallbackQuantity = pkg.getQuantity() > 0 ? pkg.getQuantity() : 1;
                    Toast.makeText(context, context.getString(R.string.error_select_order_type_first), Toast.LENGTH_SHORT).show();
                    Intent browseIntent = new Intent(context, com.example.yummyrestaurant.activities.BrowseMenuActivity.class);
                    browseIntent.putExtra("open_package_tab_after_order_type_selection", true);
                    browseIntent.putExtra("pending_reorder_package_id", pkg.getPackageId());
                    browseIntent.putExtra("pending_reorder_quantity", fallbackQuantity);
                    browseIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(browseIntent);
                    return;
                }

                MenuItem packageItem = new MenuItem();
                packageItem.setId(pkg.getPackageId());
                packageItem.setName(pkg.getPackageName());
                packageItem.setPrice(pkg.getPackagePrice() > 0 ? pkg.getPackagePrice() : pkg.getPackageCost());

                resolveMaxPackageReorderQuantity(context, packageItem, maxQuantity -> {
                    if (maxQuantity <= 0) {
                        Toast.makeText(context, context.getString(R.string.reorder_package_unavailable), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showPackageQuantityDialog(context, pkg, maxQuantity);
                }, error -> Toast.makeText(context, context.getString(R.string.reorder_material_check_failed, error), Toast.LENGTH_SHORT).show());
            } else if (order.getItems() != null && order.getItems().size() > 0) {
                // ===== 常規訂單：直接恢復到購物車 =====
                Log.d("OrderAdapter", "This is a REGULAR order with " + order.getItems().size() + " item(s)");
                CartManager.clearCart();
                Log.d("OrderAdapter", "Cart cleared");
                
                int addedCount = 0;
                for (OrderItem item : order.getItems()) {
                    if (item == null || item.getItemId() <= 0 || item.getQuantity() <= 0) {
                        Log.w("OrderAdapter", "Skipping invalid reorder item");
                        continue;
                    }

                    MenuItem menuItem = new MenuItem();
                    menuItem.setId(item.getItemId());
                    menuItem.setName(item.getName());
                    menuItem.setPrice(item.getItemPrice());
                    menuItem.setImage_url(item.getImageUrl());
                    
                    Log.d("OrderAdapter", "Processing item: " + item.getName() + 
                               " qty=" + item.getQuantity() + 
                               " customizations=" + (item.getCustomizations() != null ? item.getCustomizations().size() : 0));
                    
                    // 恢復自訂項
                    Customization customization = null;
                    if (item.getCustomizations() != null && item.getCustomizations().size() > 0) {
                        customization = new Customization();
                        List<OrderItemCustomization> customDetails = new ArrayList<>();
                        Log.d("OrderAdapter", "  Restoring " + item.getCustomizations().size() + " customization(s)");
                        
                        for (OrderItemCustomization cust : item.getCustomizations()) {
                            Log.d("OrderAdapter", "    Processing customization: optionId=" + cust.getOptionId() + 
                                       " optionName=" + cust.getOptionName() + 
                                       " choiceNames=" + cust.getChoiceNames() + 
                                       " cost=" + cust.getAdditionalCost());
                            
                            OrderItemCustomization custCopy = new OrderItemCustomization();
                            custCopy.setOptionId(cust.getOptionId());
                            custCopy.setOptionName(cust.getOptionName());
                            custCopy.setAdditionalCost(cust.getAdditionalCost());
                            
                            // 處理 choice_names（逗號分隔的字符串轉換為 List）
                            if (cust.getChoiceNames() != null && !cust.getChoiceNames().isEmpty()) {
                                String[] choices = cust.getChoiceNames().split(",\\s*");
                                List<String> choicesList = new ArrayList<>();
                                for (String choice : choices) {
                                    choicesList.add(choice.trim());
                                }
                                custCopy.setSelectedChoices(choicesList);
                                Log.d("OrderAdapter", "      ✅ Converted choiceNames to selectedChoices: " + choicesList);
                            } else {
                                Log.d("OrderAdapter", "      ❌ No choiceNames found");
                            }
                            
                            // 處理文本值（特殊要求）
                            if (cust.getTextValue() != null && !cust.getTextValue().isEmpty()) {
                                custCopy.setTextValue(cust.getTextValue());
                                Log.d("OrderAdapter", "      📝 TextValue set: " + cust.getTextValue());
                            }
                            
                            customDetails.add(custCopy);
                        }
                        
                        customization.setCustomizationDetails(customDetails);
                        Log.d("OrderAdapter", "  ✅ Customization object created with " + customDetails.size() + " details");
                    } else {
                        Log.d("OrderAdapter", "  No customizations to restore");
                    }
                    
                    // 創建 CartItem 並添加到購物車
                    CartItem cartItem = new CartItem(menuItem, customization);
                    CartManager.addItem(cartItem, item.getQuantity());
                    addedCount++;
                    Log.d("OrderAdapter", "  ✅ CartItem added: " + item.getName() + " x" + item.getQuantity());
                }

                if (addedCount == 0) {
                    Toast.makeText(context, context.getString(R.string.no_items_to_reorder), Toast.LENGTH_SHORT).show();
                    return;
                }

                MaterialAvailabilityChecker.checkCartMaterialAvailability(context, new MaterialAvailabilityChecker.MaterialCheckCallback() {
                    @Override
                    public void onCheckComplete(boolean allAvailable, String message, org.json.JSONArray materialDetails) {
                        if (allAvailable) {
                            Toast.makeText(context, context.getString(R.string.order_restored_to_cart), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, context.getString(R.string.reorder_insufficient_ingredients), Toast.LENGTH_SHORT).show();
                        }

                        Intent intent = new Intent(context, com.example.yummyrestaurant.activities.CartActivity.class);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCheckError(String error) {
                        Toast.makeText(context, context.getString(R.string.reorder_material_check_failed, error), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, com.example.yummyrestaurant.activities.CartActivity.class);
                        context.startActivity(intent);
                    }
                });
                return;
                
            } else {
                Log.d("OrderAdapter", "❌ No items found in order");
                Toast.makeText(context, context.getString(R.string.no_items_to_reorder), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("OrderAdapter", "❌ Error reordering order #" + order.getOid() + ": " + e.getMessage(), e);
            Log.e("OrderAdapter", "Stack trace:", e);
            Toast.makeText(context, context.getString(R.string.error_with_reason, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private interface QuantityResultCallback {
        void onResult(int quantity);
    }

    private interface QuantityErrorCallback {
        void onError(String error);
    }

    private void resolveMaxPackageReorderQuantity(Context context, MenuItem packageItem, QuantityResultCallback resultCallback, QuantityErrorCallback errorCallback) {
        probePackageQuantity(context, packageItem, 1, 0, resultCallback, errorCallback);
    }

    private void probePackageQuantity(Context context, MenuItem packageItem, int testQuantity, int bestQuantity, QuantityResultCallback resultCallback, QuantityErrorCallback errorCallback) {
        if (testQuantity <= 0) {
            resultCallback.onResult(bestQuantity);
            return;
        }

        if (testQuantity > MAX_PACKAGE_REORDER_QUANTITY) {
            resultCallback.onResult(bestQuantity);
            return;
        }

        MaterialAvailabilityChecker.checkItemMaterialAvailability(context, packageItem, testQuantity, true, new MaterialAvailabilityChecker.MaterialCheckCallback() {
            @Override
            public void onCheckComplete(boolean allAvailable, String message, org.json.JSONArray materialDetails) {
                if (allAvailable) {
                    int nextQuantity = testQuantity >= MAX_PACKAGE_REORDER_QUANTITY ? testQuantity : Math.min(testQuantity * 2, MAX_PACKAGE_REORDER_QUANTITY);
                    if (nextQuantity == testQuantity) {
                        resultCallback.onResult(testQuantity);
                    } else {
                        probePackageQuantity(context, packageItem, nextQuantity, testQuantity, resultCallback, errorCallback);
                    }
                    return;
                }

                if (testQuantity == 1) {
                    resultCallback.onResult(0);
                } else {
                    binarySearchPackageQuantity(context, packageItem, bestQuantity, testQuantity - 1, bestQuantity, resultCallback, errorCallback);
                }
            }

            @Override
            public void onCheckError(String error) {
                errorCallback.onError(error);
            }
        });
    }

    private void binarySearchPackageQuantity(Context context, MenuItem packageItem, int low, int high, int bestQuantity, QuantityResultCallback resultCallback, QuantityErrorCallback errorCallback) {
        if (low > high) {
            resultCallback.onResult(bestQuantity);
            return;
        }

        if (low == high) {
            MaterialAvailabilityChecker.checkItemMaterialAvailability(context, packageItem, low, true, new MaterialAvailabilityChecker.MaterialCheckCallback() {
                @Override
                public void onCheckComplete(boolean allAvailable, String message, org.json.JSONArray materialDetails) {
                    resultCallback.onResult(allAvailable ? low : bestQuantity);
                }

                @Override
                public void onCheckError(String error) {
                    errorCallback.onError(error);
                }
            });
            return;
        }

        int mid = (low + high + 1) / 2;
        MaterialAvailabilityChecker.checkItemMaterialAvailability(context, packageItem, mid, true, new MaterialAvailabilityChecker.MaterialCheckCallback() {
            @Override
            public void onCheckComplete(boolean allAvailable, String message, org.json.JSONArray materialDetails) {
                if (allAvailable) {
                    binarySearchPackageQuantity(context, packageItem, mid + 1, high, mid, resultCallback, errorCallback);
                } else {
                    binarySearchPackageQuantity(context, packageItem, low, mid - 1, bestQuantity, resultCallback, errorCallback);
                }
            }

            @Override
            public void onCheckError(String error) {
                errorCallback.onError(error);
            }
        });
    }

    private void showPackageQuantityDialog(Context context, OrderPackage pkg, int maxQuantity) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(context.getString(R.string.package_reorder_quantity_hint));
        input.setText(String.valueOf(Math.max(1, Math.min(maxQuantity, pkg.getQuantity() > 0 ? pkg.getQuantity() : 1))));

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.package_reorder_quantity_title))
                .setMessage(context.getString(R.string.package_reorder_quantity_message, maxQuantity))
                .setView(input)
                .setPositiveButton(context.getString(R.string.confirm), (dialog, which) -> {
                    int quantity = 1;
                    try {
                        quantity = Integer.parseInt(input.getText().toString().trim());
                    } catch (Exception ignored) {
                    }

                    if (quantity < 1 || quantity > maxQuantity) {
                        Toast.makeText(context, context.getString(R.string.package_reorder_quantity_invalid, maxQuantity), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    openPackageBuilder(context, pkg, quantity);
                })
                .setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openPackageBuilder(Context context, OrderPackage pkg, int quantity) {
        Intent intent = new Intent(context, com.example.yummyrestaurant.activities.BuildSetMenuActivity.class);
        intent.putExtra("package_id", pkg.getPackageId());
        intent.putExtra("is_reorder", true);
        intent.putExtra("reorder_quantity", quantity);
        Log.d("OrderAdapter", "Navigating to BuildSetMenuActivity with package_id=" + pkg.getPackageId() + ", reorder_quantity=" + quantity);
        context.startActivity(intent);

        Toast.makeText(context, context.getString(R.string.please_customize_package), Toast.LENGTH_SHORT).show();
    }

    private void showOrderDetails(Context context, Order order) {
        try {
            List<OrderItem> detailItems = buildDisplayItems(order);
            Log.d("OrderAdapter", "ℹ️ SHOWING DETAILS FOR ORDER #" + order.getOid());
            Log.d("OrderAdapter", "  Order Date: " + order.getOdate() + ", Status: " + order.getOstatus() + 
                       ", Items: " + (detailItems != null ? detailItems.size() : 0));
            
            // 創建詳細信息對話框
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.order_details_title_format, order.getOid()));
            
            // 創建滾動視圖用於詳細信息
            ScrollView scrollView = new ScrollView(context);
            LinearLayout detailsLayout = new LinearLayout(context);
            detailsLayout.setOrientation(LinearLayout.VERTICAL);
            detailsLayout.setPadding(20, 20, 20, 20);
            
            // 添加基本信息
            addDetailRow(detailsLayout, context.getString(R.string.order_detail_order_id), "#" + order.getOid());
            addDetailRow(detailsLayout, context.getString(R.string.order_detail_date), order.getOdate());
            addDetailRow(detailsLayout, context.getString(R.string.order_detail_status), getStatusText(context, order.getOstatus()));
            addDetailRow(detailsLayout, context.getString(R.string.order_detail_customer), order.getCname() != null ? order.getCname() : context.getString(R.string.not_available_short));
            addDetailRow(detailsLayout, context.getString(R.string.order_detail_table), String.valueOf(order.getTable_number()));
            
            // 添加分隔線
            View separator1 = new View(context);
            separator1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2));
            separator1.setBackgroundColor(Color.parseColor("#CCCCCC"));
            detailsLayout.addView(separator1);
            
            // 添加項目標題
            TextView itemsTitle = new TextView(context);
            itemsTitle.setText(context.getString(R.string.order_detail_items));
            itemsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            itemsTitle.setTextSize(14);
            itemsTitle.setPadding(0, 10, 0, 10);
            detailsLayout.addView(itemsTitle);
            
            // 添加常規項目
            double itemsTotal = 0;
            if (detailItems != null && detailItems.size() > 0) {
                Log.d("OrderAdapter", "  Processing " + detailItems.size() + " items for details display");
                for (OrderItem item : detailItems) {
                    double itemCost = item.getItemPrice() * item.getQuantity();
                    itemsTotal += itemCost;
                    
                    Log.d("OrderAdapter", "    Item: " + item.getName() + 
                               " qty=" + item.getQuantity() + 
                               " isPackage=" + item.isPackage() +
                               " customizations=" + (item.getCustomizations() != null ? item.getCustomizations().size() : 0));
                    
                    // Check if this is a package
                    if (item.isPackage()) {
                        // 📦 Package display with expand functionality
                        LinearLayout packageContainer = new LinearLayout(context);
                        packageContainer.setOrientation(LinearLayout.VERTICAL);
                        packageContainer.setPadding(8, 8, 8, 8);
                        packageContainer.setBackgroundColor(Color.parseColor("#F0F8FF"));
                        
                        // Package header with expand icon
                        LinearLayout packageHeader = new LinearLayout(context);
                        packageHeader.setOrientation(LinearLayout.HORIZONTAL);
                        packageHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);
                        
                        TextView expandIcon = new TextView(context);
                        expandIcon.setText("▶");
                        expandIcon.setTextSize(14);
                        expandIcon.setTypeface(null, android.graphics.Typeface.BOLD);
                        expandIcon.setTextColor(Color.parseColor("#2196F3"));
                        expandIcon.setPadding(0, 0, 8, 0);
                        packageHeader.addView(expandIcon);
                        
                        TextView packageInfo = new TextView(context);
                        String packageText = "📦 " + item.getName() + 
                                           " × " + item.getQuantity() + 
                                           "  HK$" + String.format("%.2f", item.getItemPrice()) +
                                           " = HK$" + String.format("%.2f", itemCost);
                        packageInfo.setText(packageText);
                        packageInfo.setTextSize(12);
                        packageInfo.setTypeface(null, android.graphics.Typeface.BOLD);
                        packageInfo.setPadding(0, 5, 0, 5);
                        packageHeader.addView(packageInfo);
                        
                        packageContainer.addView(packageHeader);
                        
                        // Package items container (initially hidden)
                        LinearLayout packageItemsContainer = new LinearLayout(context);
                        packageItemsContainer.setOrientation(LinearLayout.VERTICAL);
                        packageItemsContainer.setPadding(20, 8, 0, 8);
                        packageItemsContainer.setVisibility(View.GONE);
                        
                        // Add package items
                        if (item.getPackageItems() != null) {
                            Log.d("OrderAdapter", "      Package contains " + item.getPackageItems().size() + " items");
                            for (OrderItem packageItem : item.getPackageItems()) {
                                TextView packageItemView = new TextView(context);
                                packageItemView.setText("  • " + packageItem.getName());
                                packageItemView.setTextSize(11);
                                packageItemView.setPadding(0, 2, 0, 2);
                                packageItemView.setTextColor(Color.parseColor("#666666"));
                                packageItemsContainer.addView(packageItemView);
                                
                                // Display customizations for package items
                                if (packageItem.getCustomizations() != null && packageItem.getCustomizations().size() > 0) {
                                    for (OrderItemCustomization cust : packageItem.getCustomizations()) {
                                        String custText = "";
                                        if (cust.getOptionId() == SPECIAL_OPTION_ID) {
                                            custText = "      └─ " + context.getString(R.string.order_special_label) + ": " + (cust.getTextValue() != null ? cust.getTextValue() : "");
                                        } else {
                                            String choices = "";
                                            if (cust.getSelectedChoices() != null && !cust.getSelectedChoices().isEmpty()) {
                                                choices = translateOptionValues(context, String.join(", ", cust.getSelectedChoices()));
                                            } else if (cust.getChoiceNames() != null && !cust.getChoiceNames().isEmpty()) {
                                                choices = translateOptionValues(context, cust.getChoiceNamesDisplay());
                                            }
                                            custText = "      └─ " + translateOptionName(context, cust.getOptionName()) + ": " + choices;
                                        }
                                        TextView custRow = new TextView(context);
                                        custRow.setText(custText);
                                        custRow.setTextSize(10);
                                        custRow.setPadding(16, 2, 0, 2);
                                        custRow.setTextColor(Color.parseColor("#999999"));
                                        packageItemsContainer.addView(custRow);
                                    }
                                }
                            }
                        }
                        
                        packageContainer.addView(packageItemsContainer);
                        
                        // Package header click event
                        packageHeader.setOnClickListener(v -> {
                            if (packageItemsContainer.getVisibility() == View.GONE) {
                                packageItemsContainer.setVisibility(View.VISIBLE);
                                expandIcon.setText("▼");
                                Log.d("OrderAdapter", "📦 Package expanded in details dialog");
                            } else {
                                packageItemsContainer.setVisibility(View.GONE);
                                expandIcon.setText("▶");
                                Log.d("OrderAdapter", "📦 Package collapsed in details dialog");
                            }
                        });
                        
                        detailsLayout.addView(packageContainer);
                        
                    } else {
                        // Regular item display
                        String itemText = "• " + item.getName() + 
                                        " × " + item.getQuantity() + 
                                        "  HK$" + String.format("%.2f", item.getItemPrice()) +
                                        " = HK$" + String.format("%.2f", itemCost);
                        
                        TextView itemRow = new TextView(context);
                        itemRow.setText(itemText);
                        itemRow.setTextSize(12);
                        itemRow.setPadding(0, 5, 0, 5);
                        detailsLayout.addView(itemRow);
                        
                        // 顯示自訂項
                        if (item.getCustomizations() != null && item.getCustomizations().size() > 0) {
                            Log.d("OrderAdapter", "      Displaying " + item.getCustomizations().size() + " customizations");
                            for (OrderItemCustomization cust : item.getCustomizations()) {
                                String custText = "";
                                if (cust.getOptionId() == SPECIAL_OPTION_ID) {
                                    // 特殊要求
                                    custText = "   └─ " + context.getString(R.string.order_special_label) + ": " + (cust.getTextValue() != null ? cust.getTextValue() : "");
                                    Log.d("OrderAdapter", "        Special note: " + cust.getTextValue());
                                } else {
                                    // 常規自訂選項 - 優先使用 selectedChoices，備用 choiceNames
                                    String choices = "";
                                    if (cust.getSelectedChoices() != null && !cust.getSelectedChoices().isEmpty()) {
                                        choices = translateOptionValues(context, String.join(", ", cust.getSelectedChoices()));
                                    } else if (cust.getChoiceNames() != null && !cust.getChoiceNames().isEmpty()) {
                                        choices = translateOptionValues(context, cust.getChoiceNamesDisplay());
                                    }
                                    custText = "   └─ " + translateOptionName(context, cust.getOptionName()) + ": " + choices;
                                    Log.d("OrderAdapter", "        Option: " + cust.getOptionName() + " = " + choices);
                                }
                                
                                TextView custRow = new TextView(context);
                                custRow.setText(custText);
                                custRow.setTextSize(11);
                                custRow.setPadding(0, 2, 0, 2);
                                custRow.setTextColor(Color.parseColor("#666666"));
                                detailsLayout.addView(custRow);
                            }
                        }
                    }
                }
            }
            
            // 添加分隔線
            View separator2 = new View(context);
            separator2.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2));
            separator2.setBackgroundColor(Color.parseColor("#CCCCCC"));
            detailsLayout.addView(separator2);
            
            // 添加總計
            TextView totalRow = new TextView(context);
            totalRow.setText(context.getString(R.string.order_detail_total_format, itemsTotal));
            totalRow.setTypeface(null, android.graphics.Typeface.BOLD);
            totalRow.setTextSize(14);
            totalRow.setPadding(0, 10, 0, 10);
            detailsLayout.addView(totalRow);
            
            scrollView.addView(detailsLayout);
            builder.setView(scrollView);
            
            builder.setPositiveButton(context.getString(R.string.close), (dialog, which) -> dialog.dismiss());
            builder.setNegativeButton(context.getString(R.string.reorder), (dialog, which) -> {
                dialog.dismiss();
                handleReorder(context, order);
            });
            
            builder.create().show();
        } catch (Exception e) {
            Log.e("OrderAdapter", "Error showing order details", e);
            Toast.makeText(context, context.getString(R.string.error_with_reason, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void addDetailRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(parent.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 5, 0, 5);
        
        TextView labelView = new TextView(parent.getContext());
        labelView.setText(label);
        labelView.setTypeface(null, android.graphics.Typeface.BOLD);
        labelView.setTextSize(12);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(labelView);
        
        TextView valueView = new TextView(parent.getContext());
        valueView.setText(value);
        valueView.setTextSize(12);
        valueView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(valueView);
        
        parent.addView(row);
    }

    private String getStatusText(Context context, int status) {
        switch (status) {
            case 0: return context.getString(R.string.status_awaiting_cash_payment);
            case 1: return context.getString(R.string.status_pending);
            case 2: return context.getString(R.string.status_preparing);
            case 3: return context.getString(R.string.status_delivered);
            case 4: return context.getString(R.string.cancelled);
            case 5: return context.getString(R.string.status_paid);
            default: return context.getString(R.string.status_unknown);
        }
    }

    // 🔥 NEW: Create package overview display
    private LinearLayout createPackageOverview(Context context, OrderItem packageItem) {
        LinearLayout packageContainer = new LinearLayout(context);
        packageContainer.setOrientation(LinearLayout.VERTICAL);
        packageContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        packageContainer.setPadding(8, 8, 8, 8);
        packageContainer.setBackgroundColor(Color.parseColor("#F5F5F5"));
        
        // Package header
        LinearLayout packageHeader = new LinearLayout(context);
        packageHeader.setOrientation(LinearLayout.HORIZONTAL);
        packageHeader.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        packageHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        // Package expand icon
        TextView expandIcon = new TextView(context);
        expandIcon.setText("▶");
        expandIcon.setTextSize(12);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        iconParams.rightMargin = 8;
        expandIcon.setLayoutParams(iconParams);
        packageHeader.addView(expandIcon);
        
        // Package name and quantity
        TextView packageInfo = new TextView(context);
        packageInfo.setText("📦 " + PackageNameTranslator.translate(context, packageItem.getName()) + " × " + packageItem.getQuantity());
        packageInfo.setTextSize(12);
        packageInfo.setTypeface(null, android.graphics.Typeface.BOLD);
        packageInfo.setTextColor(Color.parseColor("#212121"));
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f);
        packageInfo.setLayoutParams(infoParams);
        packageHeader.addView(packageInfo);
        
        packageContainer.addView(packageHeader);
        
        // Package items container (initially hidden)
        LinearLayout itemsContainer = new LinearLayout(context);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        itemsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        itemsContainer.setPadding(24, 8, 8, 8);
        itemsContainer.setVisibility(View.GONE);
        
        // Add package items
        if (packageItem.getPackageItems() != null) {
            for (OrderItem item : packageItem.getPackageItems()) {
                TextView itemView = new TextView(context);
                itemView.setText("  • " + item.getName());
                itemView.setTextSize(11);
                itemView.setPadding(0, 2, 0, 2);
                itemView.setTextColor(Color.parseColor("#666666"));
                itemsContainer.addView(itemView);
                
                // Display customizations
                if (item.getCustomizations() != null && item.getCustomizations().size() > 0) {
                    for (OrderItemCustomization cust : item.getCustomizations()) {
                        TextView custView = new TextView(context);
                        String custText = "";
                        if (cust.getOptionId() == SPECIAL_OPTION_ID) {
                            custText = "      └─ " + context.getString(R.string.order_special_label) + ": " + (cust.getTextValue() != null ? cust.getTextValue() : "");
                        } else {
                            String choices = "";
                            if (cust.getSelectedChoices() != null && !cust.getSelectedChoices().isEmpty()) {
                                choices = translateOptionValues(context, String.join(", ", cust.getSelectedChoices()));
                            } else if (cust.getChoiceNames() != null && !cust.getChoiceNames().isEmpty()) {
                                choices = translateOptionValues(context, cust.getChoiceNamesDisplay());
                            }
                            custText = "      └─ " + translateOptionName(context, cust.getOptionName()) + ": " + choices;
                        }
                        custView.setText(custText);
                        custView.setTextSize(10);
                        custView.setPadding(16, 2, 0, 2);
                        custView.setTextColor(Color.parseColor("#999999"));
                        itemsContainer.addView(custView);
                    }
                }
            }
        }
        
        packageContainer.addView(itemsContainer);
        
        // Package header click event
        packageHeader.setOnClickListener(v -> {
            if (itemsContainer.getVisibility() == View.GONE) {
                itemsContainer.setVisibility(View.VISIBLE);
                expandIcon.setText("▼");
            } else {
                itemsContainer.setVisibility(View.GONE);
                expandIcon.setText("▶");
            }
        });
        
        return packageContainer;
    }

    // 🔥 NEW: Display all items (expanded view)
    private void displayAllItems(OrderViewHolder holder, List<OrderItem> items, Order order) {
        Log.d("OrderAdapter", "🔍 Displaying ALL " + items.size() + " items (expanded view)");
        
        for (OrderItem item : items) {
            if (item.isPackage()) {
                // Display package with all details
                LinearLayout packageContainer = createPackageOverview(holder.itemsContainer.getContext(), item);
                holder.itemsContainer.addView(packageContainer);
                
                // Auto-expand package in full view
                LinearLayout itemsContainer = (LinearLayout) packageContainer.getChildAt(1);
                TextView expandIcon = (TextView) ((LinearLayout) packageContainer.getChildAt(0)).getChildAt(0);
                itemsContainer.setVisibility(View.VISIBLE);
                expandIcon.setText("▼");
            } else {
                // Regular item with full details
                TextView itemView = new TextView(holder.itemsContainer.getContext());
                itemView.setText("• " + item.getName() + " x" + item.getQuantity());
                itemView.setTextSize(12);
                itemView.setPadding(8, 4, 8, 4);
                itemView.setTextColor(Color.parseColor("#424242"));
                holder.itemsContainer.addView(itemView);
                
                // Show all customizations
                if (item.getCustomizations() != null && item.getCustomizations().size() > 0) {
                    for (OrderItemCustomization cust : item.getCustomizations()) {
                        TextView custView = new TextView(holder.itemsContainer.getContext());
                        if (cust.getOptionId() == SPECIAL_OPTION_ID) {
                            custView.setText("    └─ " + holder.itemsContainer.getContext().getString(R.string.order_special_label) + ": " + (cust.getTextValue() != null ? cust.getTextValue() : ""));
                        } else {
                            String choices = "";
                            Context ctx = holder.itemsContainer.getContext();
                            if (cust.getSelectedChoices() != null && !cust.getSelectedChoices().isEmpty()) {
                                choices = translateOptionValues(ctx, String.join(", ", cust.getSelectedChoices()));
                            } else if (cust.getChoiceNames() != null && !cust.getChoiceNames().isEmpty()) {
                                choices = translateOptionValues(ctx, cust.getChoiceNamesDisplay());
                            }
                            custView.setText("    ├─ " + translateOptionName(ctx, cust.getOptionName()) + ": " + choices);
                        }
                        custView.setTextSize(11);
                        custView.setPadding(16, 2, 8, 2);
                        custView.setTextColor(Color.parseColor("#888888"));
                        holder.itemsContainer.addView(custView);
                    }
                }
            }
        }
        
        // Add "collapse" option
        TextView collapseView = new TextView(holder.itemsContainer.getContext());
        collapseView.setText("▲ " + holder.itemsContainer.getContext().getString(R.string.show_less));
        collapseView.setTextSize(11);
        collapseView.setPadding(8, 8, 8, 4);
        collapseView.setTextColor(Color.parseColor("#2196F3"));
        collapseView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        collapseView.setOnClickListener(v -> {
            // Refresh the view to show overview mode again
            notifyDataSetChanged();
        });
        
        holder.itemsContainer.addView(collapseView);
    }

    /**
     * Translates a customization option group name (e.g. "Spice Level") stored in
     * the database into the current locale using Android string resources.
     */
    private static String translateOptionName(Context context, String optionName) {
        if (optionName == null) return "";
        switch (optionName.trim().toLowerCase()) {
            case "spice level":  return context.getString(R.string.option_group_spice_level);
            case "sugar level":  return context.getString(R.string.option_group_sugar_level);
            case "ice level":    return context.getString(R.string.option_group_ice_level);
            case "milk level":   return context.getString(R.string.option_group_milk_level);
            case "toppings":     return context.getString(R.string.option_group_toppings);
            default:             return optionName;
        }
    }

    /**
     * Translates a comma-separated list of option value names (e.g. "Mild, Hot")
     * into the current locale using Android string resources.
     */
    private static String translateOptionValues(Context context, String values) {
        if (values == null || values.isEmpty()) return "";
        String[] parts = values.split(",\\s*");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(translateSingleValue(context, parts[i].trim()));
        }
        return sb.toString();
    }

    private static String translateSingleValue(Context context, String value) {
        if (value == null) return "";
        switch (value.trim().toLowerCase()) {
            case "mild":            return context.getString(R.string.option_value_mild);
            case "medium":          return context.getString(R.string.option_value_medium);
            case "hot":             return context.getString(R.string.option_value_hot);
            case "numbing":         return context.getString(R.string.option_value_numbing);
            case "more sweet":      return context.getString(R.string.option_value_more_sweet);
            case "less sweet":      return context.getString(R.string.option_value_less_sweet);
            case "no sweet":        return context.getString(R.string.option_value_no_sweet);
            case "more ice":        return context.getString(R.string.option_value_more_ice);
            case "less ice":        return context.getString(R.string.option_value_less_ice);
            case "no ice":          return context.getString(R.string.option_value_no_ice);
            case "more milk":       return context.getString(R.string.option_value_more_milk);
            case "less milk":       return context.getString(R.string.option_value_less_milk);
            case "no milk":         return context.getString(R.string.option_value_no_milk);
            case "extra sesame":    return context.getString(R.string.option_value_extra_sesame);
            case "peanuts":         return context.getString(R.string.option_value_peanuts);
            case "honey drizzle":   return context.getString(R.string.option_value_honey_drizzle);
            case "chocolate chips": return context.getString(R.string.option_value_chocolate_chips);
            default:                return value;
        }
    }

    private List<OrderItem> buildDisplayItems(Order order) {
        List<OrderItem> result = new ArrayList<>();

        if (order.getItems() != null) {
            result.addAll(order.getItems());
        }

        if (order.getPackages() != null && !order.getPackages().isEmpty()) {
            for (OrderPackage pkg : order.getPackages()) {
                if (pkg == null) {
                    continue;
                }

                boolean alreadyExists = false;
                for (OrderItem existing : result) {
                    if (existing != null && existing.isPackage() && existing.getPackageId() == pkg.getPackageId()) {
                        alreadyExists = true;
                        break;
                    }
                }

                if (!alreadyExists) {
                    result.add(convertPackageToOrderItem(pkg));
                }
            }
        }

        return result;
    }

    private OrderItem convertPackageToOrderItem(OrderPackage pkg) {
        int packageQty = Math.max(1, pkg.getQuantity());
        double unitPrice = pkg.getPackagePrice();

        if (unitPrice <= 0 && pkg.getPackageCost() > 0) {
            unitPrice = pkg.getPackageCost() / packageQty;
        }

        OrderItem packageItem = new OrderItem(
                pkg.getPackageId(),
                pkg.getPackageName(),
                packageQty,
                unitPrice,
                unitPrice * packageQty
        );
        packageItem.setPackage(true);
        packageItem.setPackageId(pkg.getPackageId());

        List<OrderItem> packageItems = new ArrayList<>();
        if (pkg.getDishes() != null) {
            for (OrderPackageDish dish : pkg.getDishes()) {
                if (dish == null) {
                    continue;
                }

                int dishQty = Math.max(1, dish.getQuantity());
                OrderItem dishItem = new OrderItem(
                        dish.getItemId(),
                        dish.getName(),
                        dishQty,
                        dish.getPrice(),
                        dish.getPrice() * dishQty
                );
                dishItem.setPackageItem(true);
                dishItem.setParentPackageId(pkg.getPackageId());
                dishItem.setCustomizations(dish.getCustomizations());
                packageItems.add(dishItem);
            }
        }

        packageItem.setPackageItems(packageItems);
        return packageItem;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, itemCount, totalAmount, timeAgo, statusBadge;
        LinearLayout itemsContainer;
        View statusColorBar;
        Button reorderBtn, detailsBtn;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            itemCount = itemView.findViewById(R.id.itemCount);
            totalAmount = itemView.findViewById(R.id.totalAmount);
            timeAgo = itemView.findViewById(R.id.timeAgo);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            statusColorBar = itemView.findViewById(R.id.statusColorBar);
            itemsContainer = itemView.findViewById(R.id.itemsContainer);
            reorderBtn = itemView.findViewById(R.id.reorderBtn);
            detailsBtn = itemView.findViewById(R.id.detailsBtn);
        }
    }

    /*
     * Order status codes and their meanings:
     *
orders
ostatus 0: Awaiting Cash Payment 1: Pending 2: Done 3: Paid 4: Cancelled
* Awaiting Cash Payment means customer chose cash payment and needs front desk confirmation
* Done means done preparing the food
     * These statuses help the restaurant coordinate order progress across kitchen, service, and billing.
     */
}