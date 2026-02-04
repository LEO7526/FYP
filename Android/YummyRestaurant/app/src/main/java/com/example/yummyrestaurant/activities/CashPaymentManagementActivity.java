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

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.CashPaymentTableAdapter;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.models.CashPaymentTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for staff to manage cash payments at front desk
 * Shows tables with pending cash payments and allows staff to confirm payment
 */
public class CashPaymentManagementActivity extends ThemeBaseActivity {
    private static final String TAG = "CashPaymentMgmt";
    
    private RecyclerView recyclerView;
    private CashPaymentTableAdapter adapter;
    private List<CashPaymentTable> tableList;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_payment_management);

        Log.d(TAG, "onCreate: Initializing Cash Payment Management");
        
        initializeUI();
        fetchCashPaymentTables();
    }

    private void initializeUI() {
        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Views
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.textViewEmpty);

        // Setup RecyclerView
        tableList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewCashPaymentTables);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new CashPaymentTableAdapter(this, tableList, this::onTableClicked);
        recyclerView.setAdapter(adapter);
        
        Log.d(TAG, "initializeUI: UI components initialized");
    }

    /**
     * Fetch tables with pending cash payments
     */
    private void fetchCashPaymentTables() {
        Log.d(TAG, "fetchCashPaymentTables: Loading tables with pending cash payments");
        showLoading(true);
        
        String url = ApiConstants.BASE_URL + "get_cash_payment_tables.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "API Response: " + response);
                    parseTablesResponse(response);
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

    private void parseTablesResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            
            if (jsonObject.getString("status").equals("success")) {
                JSONArray dataArray = jsonObject.getJSONArray("data");
                tableList.clear();
                
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject tableObj = dataArray.getJSONObject(i);
                    
                    CashPaymentTable table = new CashPaymentTable(
                        tableObj.getInt("table_number"),
                        tableObj.getInt("oid"),
                        tableObj.getString("customer_name"),
                        tableObj.getString("order_time"),
                        tableObj.getDouble("total_amount"),
                        tableObj.getString("items_summary")
                    );
                    
                    tableList.add(table);
                    Log.d(TAG, "Added table: " + table.getTableNumber() + " with amount: " + table.getTotalAmount());
                }
                
                adapter.notifyDataSetChanged();
                
                if (tableList.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                }
                
                Log.d(TAG, "fetchCashPaymentTables: Loaded " + tableList.size() + " tables");
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
     * Handle table click - show payment confirmation dialog
     */
    public void onTableClicked(CashPaymentTable table) {
        Log.d(TAG, "onTableClicked: Table " + table.getTableNumber() + " clicked");
        showPaymentConfirmationDialog(table);
    }

    /**
     * Show payment confirmation dialog for pending cash payments
     */
    private void showPaymentConfirmationDialog(CashPaymentTable table) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cash_payment_confirmation, null);
        builder.setView(view);
        
        TextView tvTableNumber = view.findViewById(R.id.textTableNumber);
        TextView tvCustomerName = view.findViewById(R.id.textCustomerName);
        TextView tvTotalAmount = view.findViewById(R.id.textTotalAmount);
        TextView tvItemsSummary = view.findViewById(R.id.textItemsSummary);
        Button btnConfirmPayment = view.findViewById(R.id.btnConfirmPayment);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        tvTableNumber.setText("Table: " + table.getTableNumber());
        tvCustomerName.setText("Customer: " + table.getCustomerName());
        tvTotalAmount.setText(String.format("Amount: HK$%.2f", table.getTotalAmount()));
        tvItemsSummary.setText(table.getItemsSummary());

        AlertDialog dialog = builder.create();

        btnConfirmPayment.setOnClickListener(v -> {
            Log.d(TAG, "Confirming front desk cash payment for table " + table.getTableNumber());
            processCashPayment(table.getOrderId());
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Process cash payment - update order status to confirmed
     */
    private void processCashPayment(int orderId) {
        Log.d(TAG, "processCashPayment: Processing payment for order " + orderId);
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
                Toast.makeText(this, "Front desk cash payment confirmed! Order has entered preparation stage.", Toast.LENGTH_LONG).show();
                // Refresh the table list
                fetchCashPaymentTables();
            } else {
                String message = response.optString("message", "Front desk payment confirmation failed, please try again");
                Toast.makeText(this, "Confirmation failed: " + message, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
            Toast.makeText(this, "Response parsing error", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        fetchCashPaymentTables();
    }
}