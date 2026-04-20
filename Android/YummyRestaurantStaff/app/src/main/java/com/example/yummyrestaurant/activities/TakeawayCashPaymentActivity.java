package com.example.yummyrestaurant.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.TakeawayCashOrderAdapter;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.models.TakeawayCashOrder;
import com.example.yummyrestaurant.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for staff to manage cash payments for takeaway orders.
 * Shows pending orders (ostatus=0) and today completed cash orders.
 */
public class TakeawayCashPaymentActivity extends ThemeBaseActivity {
    private static final String TAG = "TakeawayCashPayment";

    private RecyclerView recyclerView;
    private RecyclerView recyclerViewCompleted;
    private TakeawayCashOrderAdapter adapter;
    private TakeawayCashOrderAdapter completedAdapter;
    private List<TakeawayCashOrder> orderList;
    private List<TakeawayCashOrder> completedOrderList;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView pendingCountView;
    private LinearLayout emptyLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NestedScrollView takeawayScrollView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takeaway_cash_payment);

        Log.d(TAG, "onCreate: Initializing Takeaway Cash Payment Management");
        sessionManager = new SessionManager(this);
        initializeUI();
        refreshAll();
    }

    private void initializeUI() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.textViewEmpty);
        pendingCountView = findViewById(R.id.textViewPendingCount);
        emptyLayout = findViewById(R.id.layoutEmpty);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        takeawayScrollView = findViewById(R.id.takeawayScrollView);

        findViewById(R.id.btnRetry).setOnClickListener(v -> refreshAll());
        swipeRefreshLayout.setOnRefreshListener(this::refreshAll);

        // Pending RecyclerView
        orderList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewTakeawayOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TakeawayCashOrderAdapter(this, orderList, this::onOrderClicked);
        recyclerView.setAdapter(adapter);

        // Completed RecyclerView
        completedOrderList = new ArrayList<>();
        recyclerViewCompleted = findViewById(R.id.recyclerViewTakeawayCompleted);
        recyclerViewCompleted.setLayoutManager(new LinearLayoutManager(this));
        completedAdapter = new TakeawayCashOrderAdapter(this, completedOrderList, order -> { });
        recyclerViewCompleted.setAdapter(completedAdapter);
    }

    private void refreshAll() {
        fetchTakeawayCashOrders();
        fetchCompletedOrders();
    }

    private void fetchTakeawayCashOrders() {
        showLoading(true);
        String url = ApiConstants.baseUrl() + "get_takeaway_cash_orders.php?type=pending";
        Volley.newRequestQueue(this).add(new StringRequest(Request.Method.GET, url,
                response -> {
                    parseOrdersResponse(response, false);
                    showLoading(false);
                },
                error -> {
                    Log.e(TAG, "Network error: " + error.getMessage());
                    Toast.makeText(this, R.string.network_error_loading_pending_orders, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    showEmptyState(true);
                }));
    }

    private void fetchCompletedOrders() {
        String url = ApiConstants.baseUrl() + "get_takeaway_cash_orders.php?type=completed";
        Volley.newRequestQueue(this).add(new StringRequest(Request.Method.GET, url,
                response -> parseOrdersResponse(response, true),
                error -> Log.e(TAG, "Error loading completed orders: " + error.getMessage())));
    }

    private void parseOrdersResponse(String response, boolean isCompleted) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (!jsonObject.getString("status").equals("success")) return;

            JSONArray dataArray = jsonObject.getJSONArray("data");
            List<TakeawayCashOrder> target = isCompleted ? completedOrderList : orderList;
            target.clear();

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject o = dataArray.getJSONObject(i);
                target.add(new TakeawayCashOrder(
                    o.getInt("oid"),
                    o.getString("orderRef"),
                    o.getString("customer_name"),
                    o.getString("order_time"),
                    o.getDouble("total_amount"),
                    o.getString("items_summary"),
                    isCompleted
                ));
            }

            if (isCompleted) {
                completedAdapter.notifyDataSetChanged();
            } else {
                adapter.notifyDataSetChanged();
                pendingCountView.setText(getString(R.string.pending_orders_count, orderList.size()));
                showEmptyState(orderList.isEmpty() && completedOrderList.isEmpty());
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
        }
    }

    public void onOrderClicked(TakeawayCashOrder order) {
        if (order.isCompleted()) return;
        showPaymentConfirmationDialog(order);
    }

    private void showPaymentConfirmationDialog(TakeawayCashOrder order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_takeaway_cash_confirmation, null);
        builder.setView(view);

        int estimatedPoints = (int) Math.floor(order.getTotalAmount());

        ((TextView) view.findViewById(R.id.textOrderRef)).setText(getString(R.string.order_label, order.getOrderRef()));
        ((TextView) view.findViewById(R.id.textCustomerName)).setText(getString(R.string.customer_label, order.getCustomerName()));
        ((TextView) view.findViewById(R.id.textTotalAmount)).setText(getString(R.string.amount_hkd, String.format("%.2f", order.getTotalAmount())));
        ((TextView) view.findViewById(R.id.textItemsSummary)).setText(order.getItemsSummary());
        ((TextView) view.findViewById(R.id.textPointsReminder)).setText(
            getString(R.string.points_reminder_earn_after_confirmation, estimatedPoints)
        );

        AlertDialog dialog = builder.create();
        view.findViewById(R.id.btnConfirmPayment).setOnClickListener(v -> {
            processTakeawayCashPayment(order.getOrderId());
            dialog.dismiss();
        });
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void processTakeawayCashPayment(int orderId) {
        showLoading(true);
        String url = ApiConstants.baseUrl() + "process_cash_payment.php";
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("order_id", orderId);
            requestData.put("staff_id", sessionManager.getStaffId());
        } catch (JSONException e) {
            showLoading(false);
            return;
        }

        Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    handlePaymentResponse(response);
                    showLoading(false);
                },
                error -> {
                    Log.e(TAG, "Payment error: " + error.getMessage());
                    Toast.makeText(this, getString(R.string.network_error_prefix, error.getMessage()), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                }));
    }

    private void handlePaymentResponse(JSONObject response) {
        try {
            if (response.getBoolean("success")) {
                int pointsAdded = response.optInt("couponPointsAdded", 0);
                String toastMessage = pointsAdded > 0
                        ? getString(R.string.takeaway_cash_payment_confirmed_points, pointsAdded)
                        : getString(R.string.takeaway_cash_payment_confirmed);
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
                refreshAll();
            } else {
                Toast.makeText(this,
                        getString(R.string.failed_prefix, response.optString("message")),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Response parse error", e);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) return;
        takeawayScrollView.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showEmptyState(boolean show) {
        emptyLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        takeawayScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
    }
}


