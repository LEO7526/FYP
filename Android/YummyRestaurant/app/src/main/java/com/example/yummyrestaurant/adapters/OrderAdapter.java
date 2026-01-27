package com.example.yummyrestaurant.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.yummyrestaurant.utils.CartManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private static final int SPECIAL_OPTION_ID = 999; // Special requests/notes option ID
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

        // 基本信息
        holder.orderId.setText("#" + order.getOid());
        
        double total = 0;
        List<OrderItem> items = order.getItems();
        List<OrderPackage> packages = order.getPackages();
        
        Log.d("OrderAdapter", "=== BINDING ORDER #" + order.getOid() + " at position " + position + " ===");
        Log.d("OrderAdapter", "Packages: " + (packages != null ? packages.size() : "null"));
        Log.d("OrderAdapter", "Items: " + (items != null ? items.size() : "null"));

        holder.itemsContainer.removeAllViews();

        // 計數項目和計算總額
        int totalItemCount = 0;
        
        // 顯示常規菜品（如果沒有套餐）
        if (items != null && (packages == null || packages.isEmpty())) {
            Log.d("OrderAdapter", "Processing " + items.size() + " regular items (no packages)");
            for (OrderItem item : items) {
                totalItemCount += item.getQuantity();
                total += item.getItemPrice() * item.getQuantity();
                
                // 詳細的項目日誌，包含完整的自訂項檢查
                List<OrderItemCustomization> customizations = item.getCustomizations();
                int custCount = (customizations != null) ? customizations.size() : 0;
                Log.d("OrderAdapter", "  Item: " + item.getName() + 
                           " qty=" + item.getQuantity() + 
                           " price=" + item.getItemPrice() + 
                           " customizations=" + custCount);
                
                // 詳細檢查 customizations 對象
                if (customizations != null) {
                    Log.d("OrderAdapter", "    Customizations object: NOT NULL, size=" + customizations.size());
                    if (customizations.size() > 0) {
                        for (OrderItemCustomization cust : customizations) {
                            Log.d("OrderAdapter", "      ✅ Found cust: " + cust.getOptionName() + "=" + cust.getChoiceNames());
                        }
                    } else {
                        Log.d("OrderAdapter", "    ⚠️ Customizations list is EMPTY");
                    }
                } else {
                    Log.d("OrderAdapter", "    ⚠️ Customizations object is NULL");
                }
                
                TextView itemView = new TextView(holder.itemsContainer.getContext());
                itemView.setText("• " + item.getName() + " x" + item.getQuantity());
                itemView.setTextSize(12);
                itemView.setPadding(8, 4, 8, 4);
                itemView.setTextColor(Color.parseColor("#424242"));
                holder.itemsContainer.addView(itemView);
                
                // ✅ 新增：在列表中也顯示自訂項摘要
                if (item.getCustomizations() != null && item.getCustomizations().size() > 0) {
                    Log.d("OrderAdapter", "    Found " + item.getCustomizations().size() + " customizations");
                    for (OrderItemCustomization cust : item.getCustomizations()) {
                        String custDisplay = "";
                        if (cust.getOptionId() == SPECIAL_OPTION_ID) {
                            // 特殊要求
                            custDisplay = "Special: " + (cust.getTextValue() != null ? cust.getTextValue() : "");
                        } else {
                            // 常規自訂項 - 優先使用 selectedChoices，備用 choiceNames
                            String choices = "";
                            if (cust.getSelectedChoices() != null && !cust.getSelectedChoices().isEmpty()) {
                                choices = String.join(", ", cust.getSelectedChoices());
                            } else if (cust.getChoiceNames() != null && !cust.getChoiceNames().isEmpty()) {
                                choices = cust.getChoiceNamesDisplay();
                            }
                            custDisplay = cust.getOptionName() + ": " + choices;
                        }
                        Log.d("OrderAdapter", "      - " + custDisplay);
                        
                        TextView custView = new TextView(holder.itemsContainer.getContext());
                        if (cust.getOptionId() == SPECIAL_OPTION_ID) {
                            custView.setText("    └─ Special: " + (cust.getTextValue() != null ? cust.getTextValue() : ""));
                        } else {
                            // 優先使用 selectedChoices 顯示
                            String choices = "";
                            if (cust.getSelectedChoices() != null && !cust.getSelectedChoices().isEmpty()) {
                                choices = String.join(", ", cust.getSelectedChoices());
                            } else if (cust.getChoiceNames() != null && !cust.getChoiceNames().isEmpty()) {
                                choices = cust.getChoiceNamesDisplay();
                            }
                            custView.setText("    ├─ " + cust.getOptionName() + ": " + choices);
                        }
                        custView.setTextSize(11);
                        custView.setPadding(16, 2, 8, 2);
                        custView.setTextColor(Color.parseColor("#888888"));
                        holder.itemsContainer.addView(custView);
                    }
                }
            }
        } else if (items != null) {
            // 有套餐的情況，跳過顯示 items（已在套餐詳情中）
            Log.d("OrderAdapter", "Skipping " + items.size() + " items display - order has packages");
        }

        // 顯示套餐
        if (packages != null && packages.size() > 0) {
            Log.d("OrderAdapter", "Displaying " + packages.size() + " packages");
            for (OrderPackage pkg : packages) {
                totalItemCount++;
                total += pkg.getPackageCost();
                
                // 創建套餐容器
                LinearLayout packageContainer = new LinearLayout(holder.itemsContainer.getContext());
                packageContainer.setOrientation(LinearLayout.VERTICAL);
                packageContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                packageContainer.setPadding(8, 8, 8, 8);
                packageContainer.setBackgroundColor(Color.parseColor("#F5F5F5"));
                
                // 套餐標題行
                LinearLayout packageHeader = new LinearLayout(holder.itemsContainer.getContext());
                packageHeader.setOrientation(LinearLayout.HORIZONTAL);
                packageHeader.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                packageHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);
                
                // 套餐圖標
                TextView packageIcon = new TextView(holder.itemsContainer.getContext());
                packageIcon.setText("");
                packageIcon.setTextSize(16);
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                iconParams.rightMargin = 8;
                packageIcon.setLayoutParams(iconParams);
                packageHeader.addView(packageIcon);
                
                // 套餐名稱和數量
                TextView packageInfo = new TextView(holder.itemsContainer.getContext());
                packageInfo.setText(pkg.getPackageName() + " × " + pkg.getQuantity());
                packageInfo.setTextSize(13);
                packageInfo.setTypeface(null, android.graphics.Typeface.BOLD);
                packageInfo.setTextColor(Color.parseColor("#212121"));
                LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1.0f);
                packageInfo.setLayoutParams(infoParams);
                packageHeader.addView(packageInfo);
                
                // 套餐價格
                TextView packagePrice = new TextView(holder.itemsContainer.getContext());
                packagePrice.setText("$" + String.format("%.2f", pkg.getPackageCost()));
                packagePrice.setTextSize(13);
                packagePrice.setTypeface(null, android.graphics.Typeface.BOLD);
                packagePrice.setTextColor(Color.parseColor("#FF6F00"));
                LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                priceParams.leftMargin = 8;
                packagePrice.setLayoutParams(priceParams);
                packageHeader.addView(packagePrice);
                
                packageContainer.addView(packageHeader);
                
                // 菜品列表（初始隱藏）
                LinearLayout dishesContainer = new LinearLayout(holder.itemsContainer.getContext());
                dishesContainer.setOrientation(LinearLayout.VERTICAL);
                dishesContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                dishesContainer.setPadding(24, 8, 8, 8);
                dishesContainer.setVisibility(View.GONE);
                
                // 添加菜品
                if (pkg.getDishes() != null) {
                    for (OrderPackageDish dish : pkg.getDishes()) {
                        TextView dishView = new TextView(holder.itemsContainer.getContext());
                        dishView.setText("• " + dish.getName());
                        dishView.setTextSize(11);
                        dishView.setPadding(0, 2, 0, 2);
                        dishView.setTextColor(Color.parseColor("#666666"));
                        dishesContainer.addView(dishView);
                        
                        // Display customizations for package dish
                        if (dish.getCustomizations() != null && dish.getCustomizations().size() > 0) {
                            for (OrderItemCustomization cust : dish.getCustomizations()) {
                                TextView custView = new TextView(holder.itemsContainer.getContext());
                                String custText = "";
                                if (cust.getOptionId() == SPECIAL_OPTION_ID) {
                                    custText = "    └─ Special: " + (cust.getTextValue() != null ? cust.getTextValue() : "");
                                } else {
                                    String choices = "";
                                    if (cust.getSelectedValues() != null && !cust.getSelectedValues().isEmpty()) {
                                        choices = String.join(", ", cust.getSelectedValues());
                                    } else if (cust.getSelectedChoices() != null && !cust.getSelectedChoices().isEmpty()) {
                                        choices = String.join(", ", cust.getSelectedChoices());
                                    } else if (cust.getChoiceNames() != null && !cust.getChoiceNames().isEmpty()) {
                                        choices = cust.getChoiceNamesDisplay();
                                    }
                                    custText = "    └─ " + cust.getGroupName() + ": " + choices;
                                }
                                custView.setText(custText);
                                custView.setTextSize(10);
                                custView.setPadding(16, 2, 0, 2);
                                custView.setTextColor(Color.parseColor("#999999"));
                                dishesContainer.addView(custView);
                            }
                        }
                    }
                }
                
                packageContainer.addView(dishesContainer);
                
                // 套餐頭部點擊事件
                packageHeader.setOnClickListener(v -> {
                    if (dishesContainer.getVisibility() == View.GONE) {
                        dishesContainer.setVisibility(View.VISIBLE);
                        packageIcon.setText("▼");
                    } else {
                        dishesContainer.setVisibility(View.GONE);
                        packageIcon.setText("");
                    }
                });
                
                holder.itemsContainer.addView(packageContainer);
            }
        }

        // 設置項目計數
        holder.itemCount.setText(totalItemCount + " item" + (totalItemCount > 1 ? "s" : ""));
        Log.d("OrderAdapter", "Total item count: " + totalItemCount);
        
        // 設置總金額
        holder.totalAmount.setText(String.format("HK$%.2f", total));
        Log.d("OrderAdapter", "Total amount: HK$" + String.format("%.2f", total));

        // 設置時間戳
        String timeAgoText = formatTimeAgo(order.getOdate());
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

    private String formatTimeAgo(String orderDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(orderDate);
            long timeAgo = System.currentTimeMillis() - date.getTime();
            
            if (timeAgo < 60000) {
                return "Just now";
            } else if (timeAgo < 3600000) {
                long minutes = timeAgo / 60000;
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else if (timeAgo < 86400000) {
                long hours = timeAgo / 3600000;
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else {
                long days = timeAgo / 86400000;
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            }
        } catch (ParseException e) {
            return orderDate;
        }
    }

    private void setStatusBadge(OrderViewHolder holder, int status) {
        String statusText;
        int backgroundColor;
        
        switch (status) {
            case 1:
                statusText = "Pending";
                backgroundColor = Color.parseColor("#FFC107"); // 黃色
                break;
            case 2:
                statusText = "Preparing";
                backgroundColor = Color.parseColor("#2196F3"); // 藍色
                break;
            case 3:
                statusText = "Delivered";
                backgroundColor = Color.parseColor("#4CAF50"); // 綠色
                break;
            case 4:
                statusText = "Cancelled";
                backgroundColor = Color.parseColor("#F44336"); // 紅色
                break;
            case 5:
                statusText = "Paid";
                backgroundColor = Color.parseColor("#4CAF50"); // 綠色
                break;
            default:
                statusText = "Unknown";
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
                
                // 導航到 BuildSetMenuActivity
                Intent intent = new Intent(context, com.example.yummyrestaurant.activities.BuildSetMenuActivity.class);
                intent.putExtra("package_id", pkg.getPackageId());
                intent.putExtra("is_reorder", true);
                Log.d("OrderAdapter", "Navigating to BuildSetMenuActivity with package_id=" + pkg.getPackageId());
                context.startActivity(intent);
                
                Toast.makeText(context, "Please customize your package", Toast.LENGTH_SHORT).show();
            } else if (order.getItems() != null && order.getItems().size() > 0) {
                // ===== 常規訂單：直接恢復到購物車 =====
                Log.d("OrderAdapter", "This is a REGULAR order with " + order.getItems().size() + " item(s)");
                CartManager.clearCart();
                Log.d("OrderAdapter", "Cart cleared");
                
                int addedCount = 0;
                for (OrderItem item : order.getItems()) {
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
                    Log.d("OrderAdapter", "  ✅ CartItem added: " + item.getName() + " x" + item.getQuantity());
                }
                
                Toast.makeText(context, "Order restored to cart!", Toast.LENGTH_SHORT).show();
                Log.d("OrderAdapter", "✅ ALL ITEMS RESTORED TO CART - Ready for reorder");
                
                // 導航到購物車
                Log.d("OrderAdapter", "🔄 Navigating to CartActivity...");
                Intent intent = new Intent(context, com.example.yummyrestaurant.activities.CartActivity.class);
                context.startActivity(intent);
                Log.d("OrderAdapter", "✅ CartActivity started");
            } else {
                Log.d("OrderAdapter", "❌ No items found in order");
                Toast.makeText(context, "No items to reorder", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("OrderAdapter", "❌ Error reordering order #" + order.getOid() + ": " + e.getMessage(), e);
            Log.e("OrderAdapter", "Stack trace:", e);
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showOrderDetails(Context context, Order order) {
        try {
            Log.d("OrderAdapter", "ℹ️ SHOWING DETAILS FOR ORDER #" + order.getOid());
            Log.d("OrderAdapter", "  Order Date: " + order.getOdate() + ", Status: " + order.getOstatus() + 
                       ", Items: " + (order.getItems() != null ? order.getItems().size() : 0));
            
            // 創建詳細信息對話框
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Order Details - #" + order.getOid());
            
            // 創建滾動視圖用於詳細信息
            ScrollView scrollView = new ScrollView(context);
            LinearLayout detailsLayout = new LinearLayout(context);
            detailsLayout.setOrientation(LinearLayout.VERTICAL);
            detailsLayout.setPadding(20, 20, 20, 20);
            
            // 添加基本信息
            addDetailRow(detailsLayout, "Order ID:", "#" + order.getOid());
            addDetailRow(detailsLayout, "Date:", order.getOdate());
            addDetailRow(detailsLayout, "Status:", getStatusText(order.getOstatus()));
            addDetailRow(detailsLayout, "Customer:", order.getCname() != null ? order.getCname() : "N/A");
            addDetailRow(detailsLayout, "Table:", String.valueOf(order.getTable_number()));
            
            // 添加分隔線
            View separator1 = new View(context);
            separator1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2));
            separator1.setBackgroundColor(Color.parseColor("#CCCCCC"));
            detailsLayout.addView(separator1);
            
            // 添加項目標題
            TextView itemsTitle = new TextView(context);
            itemsTitle.setText("Items:");
            itemsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            itemsTitle.setTextSize(14);
            itemsTitle.setPadding(0, 10, 0, 10);
            detailsLayout.addView(itemsTitle);
            
            // 添加常規項目
            double itemsTotal = 0;
            if (order.getItems() != null && order.getItems().size() > 0) {
                Log.d("OrderAdapter", "  Processing " + order.getItems().size() + " items for details display");
                for (OrderItem item : order.getItems()) {
                    double itemCost = item.getItemPrice() * item.getQuantity();
                    itemsTotal += itemCost;
                    
                    Log.d("OrderAdapter", "    Item: " + item.getName() + 
                               " qty=" + item.getQuantity() + 
                               " customizations=" + (item.getCustomizations() != null ? item.getCustomizations().size() : 0));
                    
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
                                custText = "   └─ Special: " + cust.getTextValue();
                                Log.d("OrderAdapter", "        Special note: " + cust.getTextValue());
                            } else {
                                // 常規自訂選項 - 優先使用 selectedChoices，備用 choiceNames
                                String choices = "";
                                if (cust.getSelectedChoices() != null && !cust.getSelectedChoices().isEmpty()) {
                                    choices = String.join(", ", cust.getSelectedChoices());
                                } else if (cust.getChoiceNames() != null && !cust.getChoiceNames().isEmpty()) {
                                    choices = cust.getChoiceNamesDisplay();
                                }
                                custText = "   └─ " + cust.getOptionName() + ": " + choices;
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
            } else if (order.getPackages() != null && order.getPackages().size() > 0) {
                // 添加套餐
                for (OrderPackage pkg : order.getPackages()) {
                    itemsTotal += pkg.getPackageCost();
                    
                    String pkgText = " " + pkg.getPackageName() +
                                   " × " + pkg.getQuantity() + 
                                   " = HK$" + String.format("%.2f", pkg.getPackageCost());
                    
                    TextView pkgRow = new TextView(context);
                    pkgRow.setText(pkgText);
                    pkgRow.setTypeface(null, android.graphics.Typeface.BOLD);
                    pkgRow.setTextSize(12);
                    pkgRow.setPadding(0, 5, 0, 5);
                    detailsLayout.addView(pkgRow);
                    
                    // 添加菜品
                    if (pkg.getDishes() != null) {
                        for (OrderPackageDish dish : pkg.getDishes()) {
                            String dishText = "  ├─ " + dish.getName();
                            TextView dishRow = new TextView(context);
                            dishRow.setText(dishText);
                            dishRow.setTextSize(11);
                            dishRow.setPadding(20, 2, 0, 2);
                            detailsLayout.addView(dishRow);
                            
                            // Display customizations for package dish in details
                            if (dish.getCustomizations() != null && dish.getCustomizations().size() > 0) {
                                Log.d("OrderAdapter", "      Package dish has " + dish.getCustomizations().size() + " customizations");
                                for (OrderItemCustomization cust : dish.getCustomizations()) {
                                    String custText = "";
                                    if (cust.getOptionId() == SPECIAL_OPTION_ID) {
                                        custText = "     └─ Special: " + cust.getTextValue();
                                        Log.d("OrderAdapter", "        Special note: " + cust.getTextValue());
                                    } else {
                                        String choices = "";
                                        if (cust.getSelectedValues() != null && !cust.getSelectedValues().isEmpty()) {
                                            choices = String.join(", ", cust.getSelectedValues());
                                        } else if (cust.getSelectedChoices() != null && !cust.getSelectedChoices().isEmpty()) {
                                            choices = String.join(", ", cust.getSelectedChoices());
                                        } else if (cust.getChoiceNames() != null && !cust.getChoiceNames().isEmpty()) {
                                            choices = cust.getChoiceNamesDisplay();
                                        }
                                        custText = "     └─ " + cust.getGroupName() + ": " + choices;
                                        Log.d("OrderAdapter", "        Option: " + cust.getGroupName() + " = " + choices);
                                    }
                                    
                                    TextView custRow = new TextView(context);
                                    custRow.setText(custText);
                                    custRow.setTextSize(10);
                                    custRow.setPadding(30, 2, 0, 2);
                                    custRow.setTextColor(Color.parseColor("#888888"));
                                    detailsLayout.addView(custRow);
                                }
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
            totalRow.setText("Total: HK$" + String.format("%.2f", itemsTotal));
            totalRow.setTypeface(null, android.graphics.Typeface.BOLD);
            totalRow.setTextSize(14);
            totalRow.setPadding(0, 10, 0, 10);
            detailsLayout.addView(totalRow);
            
            scrollView.addView(detailsLayout);
            builder.setView(scrollView);
            
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            builder.setNegativeButton("Reorder", (dialog, which) -> {
                dialog.dismiss();
                handleReorder(context, order);
            });
            
            builder.create().show();
        } catch (Exception e) {
            Log.e("OrderAdapter", "Error showing order details", e);
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private String getStatusText(int status) {
        switch (status) {
            case 1: return "Pending";
            case 2: return "Preparing";
            case 3: return "Delivered";
            case 4: return "Cancelled";
            case 5: return "Paid";
            default: return "Unknown";
        }
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
     * 1 - Pending: Order received
     * 2 - Cancelled: Order was cancelled — used by system to record cancellations.
     * 3 - Paid: Bill has been settled — used by cashier or POS system to confirm payment.
     *
     * These statuses help the restaurant coordinate order progress across kitchen, service, and billing.
     */
}