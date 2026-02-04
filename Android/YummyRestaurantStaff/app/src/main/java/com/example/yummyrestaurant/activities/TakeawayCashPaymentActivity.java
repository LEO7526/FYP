package com.example.yummyrestaurant.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.TakeawayCashOrderAdapter;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.models.TakeawayCashOrder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for staff to manage cash payments for takeaway orders
 * Shows takeaway orders with pending cash payments (ostatus=0) and allows staff to confirm payment
 */
public class TakeawayCashPaymentActivity extends androidx.appcompat.app.AppCompatActivity {
    private static final String TAG = "TakeawayCashPayment";
    
    private RecyclerView recyclerView;
    private TakeawayCashOrderAdapter adapter;
    private List<TakeawayCashOrder> orderList;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takeaway_cash_payment);

        Log.d(TAG, "onCreate: Initializing Takeaway Cash Payment Management");
        
        initializeUI();
        fetchTakeawayCashOrders();
    }

    private void initializeUI() {
        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Views
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.textViewEmpty);

        // Setup RecyclerView
        orderList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewTakeawayOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // For now, create a simple adapter
        // adapter = new TakeawayCashOrderAdapter(this, orderList, this::onOrderClicked);
        // recyclerView.setAdapter(adapter);
        
        Log.d(TAG, "initializeUI: UI components initialized");
    }

    /**
     * Fetch takeaway orders with pending cash payments (ostatus=0)
     */
    private void fetchTakeawayCashOrders() {
        Log.d(TAG, "fetchTakeawayCashOrders: Loading takeaway orders with pending cash payments");
        showLoading(true);
        
        String url = ApiConstants.BASE_URL + "get_takeaway_cash_orders.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "API Response: " + response);
                    parseOrdersResponse(response);
                    showLoading(false);
                },
                error -> {
                    Log.e(TAG, "Network error: " + error.getMessage());
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    showEmptyState(true);
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void parseOrdersResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            
            if (jsonObject.getString("status").equals("success")) {
                JSONArray dataArray = jsonObject.getJSONArray("data");
                orderList.clear();
                
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject orderObj = dataArray.getJSONObject(i);
                    
                    TakeawayCashOrder order = new TakeawayCashOrder(
                        orderObj.getInt("oid"),
                        orderObj.getString("orderRef"),
                        orderObj.getString("customer_name"),
                        orderObj.getString("order_time"),
                        orderObj.getDouble("total_amount"),
                        orderObj.getString("items_summary")
                    );
                    
                    orderList.add(order);
                    Log.d(TAG, "Added order: " + order.getOrderRef() + " with amount: " + order.getTotalAmount());
                }
                
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                
                if (orderList.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                }
                
                Log.d(TAG, "fetchTakeawayCashOrders: Loaded " + orderList.size() + " orders");
            } else {
                String message = jsonObject.optString("message", "Unknown error");
                Log.w(TAG, "API Error: " + message);
                Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
            Toast.makeText(this, "Data parsing error", Toast.LENGTH_SHORT).show();
            showEmptyState(true);
        }
    }

    /**
     * Handle order click - show payment confirmation dialog
     */
    public void onOrderClicked(TakeawayCashOrder order) {
        Log.d(TAG, "onOrderClicked: Order " + order.getOrderRef() + " clicked");
        showPaymentConfirmationDialog(order);
    }

    /**
     * Show payment confirmation dialog for pending takeaway cash payments
     */
    private void showPaymentConfirmationDialog(TakeawayCashOrder order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setTitle("Confirm Cash Payment");
        builder.setMessage("Order: " + order.getOrderRef() + "\n" +
                          "Customer: " + order.getCustomerName() + "\n" +
                          "Amount: HK$" + String.format("%.2f", order.getTotalAmount()) + "\n\n" +
                          order.getItemsSummary());

        builder.setPositiveButton("Confirm Payment", (dialog, which) -> {
            Log.d(TAG, "Confirming takeaway cash payment for order " + order.getOrderRef());
            processTakeawayCashPayment(order.getOrderId());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Process takeaway cash payment - update order status to confirmed
     */
    private void processTakeawayCashPayment(int orderId) {
        Log.d(TAG, "processTakeawayCashPayment: Processing payment for order " + orderId);
        showLoading(true);
        
        String url = ApiConstants.BASE_URL + "process_cash_payment.php";

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("order_id", orderId);
            requestData.put("staff_id", 1); // TODO: Get from current staff session
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error", e);
            showLoading(false);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    Log.d(TAG, "Payment processing response: " + response.toString());
                    handlePaymentResponse(response);
                    showLoading(false);
                },
                error -> {
                    Log.e(TAG, "Payment processing error: " + error.getMessage());
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void handlePaymentResponse(JSONObject response) {
        try {
            if (response.getBoolean("success")) {
                Toast.makeText(this, "Takeaway cash payment confirmed! Order ready for preparation.", Toast.LENGTH_LONG).show();
                // Refresh the order list
                fetchTakeawayCashOrders();
            } else {
                String message = response.optString("message", "Payment confirmation failed, please try again");
                Toast.makeText(this, "Confirmation failed: " + message, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
            Toast.makeText(this, "Response parsing error", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyState(boolean show) {
        if (emptyView != null) {
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        fetchTakeawayCashOrders();
    }
}