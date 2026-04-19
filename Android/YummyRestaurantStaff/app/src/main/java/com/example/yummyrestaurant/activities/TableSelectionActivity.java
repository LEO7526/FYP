package com.example.yummyrestaurant.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.models.Table;
import com.example.yummyrestaurant.views.SeatingChartView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TableSelectionActivity extends StaffBaseActivity implements SeatingChartView.OnTableSelectedListener {

    private String allDayMode;

    private SeatingChartView seatingChartView;
    private List<Table> tableList;
    private AlertDialog tableDetailDialog;
    private Button btnFilterDate;
    private Button btnFilterTime;
    private Button btnApplySlot;
    private TextView tvFilterSlot;
    private String selectedDate;
    private String selectedTime;
    private String[] timeSlots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_select_table);
        allDayMode = getString(R.string.all_day);

        // 1. 綁定返回按鈕
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 2. 初始化座位圖
        tableList = new ArrayList<>();
        seatingChartView = findViewById(R.id.seatingChartView);
        seatingChartView.setOnTableSelectedListener(this);

        btnFilterDate = findViewById(R.id.btnFilterDate);
        btnFilterTime = findViewById(R.id.btnFilterTime);
        btnApplySlot = findViewById(R.id.btnApplySlot);
        tvFilterSlot = findViewById(R.id.tvFilterSlot);

        Calendar now = Calendar.getInstance();
        selectedDate = String.format(Locale.US, "%04d-%02d-%02d",
                now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
        timeSlots = buildTimeSlots();
        selectedTime = getNearestTimeSlot(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));

        btnFilterDate.setText(selectedDate);
        btnFilterTime.setText(selectedTime);
        updateSlotSummary();

        btnFilterDate.setOnClickListener(v -> showDatePicker());
        btnFilterTime.setOnClickListener(v -> showTimeSlotPicker());
        btnApplySlot.setOnClickListener(v -> fetchTableStatus());

        // 3. 載入桌位狀態
        fetchTableStatus();
    }

    private String[] buildTimeSlots() {
        List<String> slots = new ArrayList<>();
        slots.add(allDayMode);
        for (int h = 11; h <= 22; h++) {
            slots.add(String.format(Locale.US, "%02d:00", h));
            if (h != 22) {
                slots.add(String.format(Locale.US, "%02d:30", h));
            }
        }
        return slots.toArray(new String[0]);
    }

    private String getNearestTimeSlot(int h, int m) {
        int target = h * 60 + m;
        String nearest = "11:00";
        int best = Integer.MAX_VALUE;
        for (String slot : timeSlots) {
            if (allDayMode.equals(slot)) {
                continue;
            }
            String[] p = slot.split(":");
            int mins = Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
            int diff = Math.abs(mins - target);
            if (diff < best) {
                best = diff;
                nearest = slot;
            }
        }
        return nearest;
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, mo, d) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", y, mo + 1, d);
            btnFilterDate.setText(selectedDate);
            updateSlotSummary();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void showTimeSlotPicker() {
        int checked = 0;
        for (int i = 0; i < timeSlots.length; i++) {
            if (timeSlots[i].equals(selectedTime)) {
                checked = i;
                break;
            }
        }
        final int[] selected = {checked};
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_time)
                .setSingleChoiceItems(timeSlots, checked, (dialog, which) -> selected[0] = which)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    selectedTime = timeSlots[selected[0]];
                    btnFilterTime.setText(selectedTime);
                    updateSlotSummary();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateSlotSummary() {
        if (tvFilterSlot != null) {
            if (allDayMode.equals(selectedTime)) {
                tvFilterSlot.setText(getString(R.string.slot_all_day_format, selectedDate));
            } else {
                tvFilterSlot.setText(getString(R.string.slot_time_format, selectedDate, selectedTime));
            }
        }
    }

    private boolean isAllDayMode() {
        return allDayMode.equals(selectedTime);
    }

    private void fetchTableStatus() {
        String url;
        try {
            if (isAllDayMode()) {
                url = ApiConstants.BASE_URL + "get_table_status.php?date="
                        + URLEncoder.encode(selectedDate, "UTF-8")
                        + "&view_mode=day";
            } else {
                url = ApiConstants.BASE_URL + "get_table_status.php?date="
                        + URLEncoder.encode(selectedDate, "UTF-8")
                        + "&time=" + URLEncoder.encode(selectedTime, "UTF-8");
            }
        } catch (Exception e) {
            url = ApiConstants.BASE_URL + "get_table_status.php";
        }
        
        android.util.Log.d("TableSelection", "Fetching table status from: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    android.util.Log.d("TableSelection", "API Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            tableList.clear();

                            android.util.Log.d("TableSelection", "Number of tables received: " + data.length());

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                int id = obj.getInt("id");
                                int capacity = obj.optInt("capacity", 0);
                                int statusCode = obj.getInt("status");
                                float x = (float) obj.optDouble("x", 0);
                                float y = (float) obj.optDouble("y", 0);

                                String status = "available";
                                if (statusCode == 2) {
                                    status = "occupied";
                                } else if (statusCode == 1) {
                                    status = "reserved";
                                }

                                Table table = new Table(
                                        id,
                                        capacity,
                                        x,
                                        y,
                                        status,
                                        statusCode == 0,
                                        true
                                );
                                tableList.add(table);
                                android.util.Log.d("TableSelection", "Added table - ID: " + table.getTid() + ", x=" + table.getX() + ", y=" + table.getY() + ", status=" + table.getStatus());
                            }
                            seatingChartView.setTables(tableList);
                            android.util.Log.d("TableSelection", "Seating chart updated with " + tableList.size() + " tables");
                        } else {
                            String errorMessage = jsonObject.optString("message", "Unknown error");
                            android.util.Log.e("TableSelection", "API Error: " + errorMessage);
                            Toast.makeText(this, getString(R.string.error_prefix, errorMessage), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("TableSelection", "JSON parsing error", e);
                        Toast.makeText(this, getString(R.string.data_parsing_error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    android.util.Log.e("TableSelection", "Network error", error);
                    Toast.makeText(this, getString(R.string.network_error_prefix, error.getMessage()), Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onTableSelected(Table table) {
        showTableDetails(table.getTid());
    }

    @Override
    public void onTableUnavailable(int tableId) {
        showTableDetails(tableId);
    }

    // Backward-compatible entry point for existing adapters
    public void onTableClicked(int tableId) {
        showTableDetails(tableId);
    }

    // 顯示詳情 (已移除對 detailStatus 的依賴)
    private void showTableDetails(int tableId) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        // Avoid multiple dialog windows stacking and leaking on fast taps.
        if (tableDetailDialog != null && tableDetailDialog.isShowing()) {
            tableDetailDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_table_details, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        tableDetailDialog = dialog;
        dialog.setOnDismissListener(d -> tableDetailDialog = null);

        // 1. 綁定 UI (注意：這裡不抓取 detailStatus，因為 XML 已移除)
        TextView title = view.findViewById(R.id.detailTableTitle);
        TextView customer = view.findViewById(R.id.detailCustomer);
        TextView time = view.findViewById(R.id.detailTime);
        TextView items = view.findViewById(R.id.detailItems);
        Button btnClose = view.findViewById(R.id.btnCloseDetail);

        // 2. 初始化
        title.setText(getString(R.string.table_title, tableId));
        customer.setText(getString(R.string.loading));
        time.setText(getString(R.string.loading));
        items.setText(getString(R.string.checking_data));

        btnClose.setOnClickListener(v -> dialog.dismiss());
        if (!isFinishing() && !isDestroyed()) {
            dialog.show();
        }

        // 3. API 請求詳情
        String url;
        try {
            url = ApiConstants.BASE_URL + "get_table_detail.php?tid=" + tableId
                    + "&date=" + URLEncoder.encode(selectedDate, "UTF-8");

            if (isAllDayMode()) {
                url += "&view_mode=day";
            } else {
                url += "&time=" + URLEncoder.encode(selectedTime, "UTF-8");
            }
        } catch (Exception e) {
            url = ApiConstants.BASE_URL + "get_table_detail.php?tid=" + tableId;
        }

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (isFinishing() || isDestroyed() || tableDetailDialog == null || !tableDetailDialog.isShowing()) {
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.has("status") || !json.getString("status").equals("success")) {
                            items.setText(R.string.no_data_available_dot);
                            return;
                        }

                        JSONObject data = json.optJSONObject("data");
                        if (data == null) return;

                        String type = data.optString("type", "unknown");
                        String name = data.optString("customer_name", getString(R.string.none));
                        customer.setText(name);

                        // 判斷狀態類型並顯示
                        if (type.equals("live")) {
                            // 現場用餐中
                            int min = data.optInt("duration", 0);
                            boolean isFutureOrder = data.optBoolean("is_future_order", false);
                            String orderTime = data.optString("order_time", "-");
                            if (isFutureOrder) {
                                time.setText(getString(R.string.order_at_time, orderTime));
                            } else {
                                time.setText(min > 1440
                                        ? getString(R.string.days_ago, (min / 1440))
                                        : getString(R.string.mins_ago, min));
                            }

                            JSONArray itemArray = data.optJSONArray("items");
                            if (itemArray != null && itemArray.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < itemArray.length(); i++) {
                                    sb.append("• ").append(itemArray.optString(i)).append("\n");
                                }
                                items.setText(sb.toString());
                            } else {
                                items.setText(R.string.no_items_ordered_yet);
                            }
                        } else if (type.equals("booking") || type.equals("booking_schedule")) {
                            // 同桌多時段預約列表
                            int totalBookings = data.optInt("total_bookings", 0);
                            time.setText(getString(R.string.booking_today_count, totalBookings));

                            JSONArray bookingArray = data.optJSONArray("bookings");
                            if (bookingArray != null && bookingArray.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < bookingArray.length(); i++) {
                                    JSONObject b = bookingArray.optJSONObject(i);
                                    if (b == null) {
                                        continue;
                                    }
                                    String btime = b.optString("btime", "-");
                                    String bname = b.optString("customer_name", getString(R.string.unknown));
                                    int pax = b.optInt("pnum", 0);
                                    String statusText = b.optString("booking_status_text", getString(R.string.reserved));
                                    sb.append(getString(R.string.booking_line_format, btime, bname, pax, statusText));
                                }
                                items.setText(sb.toString().trim());
                            } else {
                                items.setText(R.string.no_booking_details);
                            }

                            customer.setText(R.string.booking_schedule);
                        } else {
                            // 空桌
                            time.setText("-");
                            items.setText(R.string.table_currently_available);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        items.setText(R.string.error_parsing_data);
                    }
                },
                error -> {
                    if (isFinishing() || isDestroyed() || tableDetailDialog == null || !tableDetailDialog.isShowing()) {
                        return;
                    }
                    items.setText(getString(R.string.net_error_prefix, error.getMessage()));
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tableDetailDialog != null && tableDetailDialog.isShowing()) {
            tableDetailDialog.dismiss();
        }
    }
}