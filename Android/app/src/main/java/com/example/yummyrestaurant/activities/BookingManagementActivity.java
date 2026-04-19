package com.example.yummyrestaurant.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.BookingAdapter;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.models.Booking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * BookingManagementActivity - Staff booking check-in and payment management
 * 
 * Features:
 * - Display all bookings for today
 * - Show table position and booking details
 * - Check-in functionality (mark customer as arrived)
 * - Proceed to cash payment after check-in
 * 
 * UI Flow:
 * 1. Swipe to refresh list of bookings
 * 2. Click booking to view details
 * 3. Click "Check-in" to mark customer arrived
 * 4. Click "Process Payment" to handle cash payment
 */
public class BookingManagementActivity extends ThemeBaseActivity {
    private static final String TAG = "BookingManagement";
    
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private ProgressBar progressBar;
    private TextView emptyView;
    private LinearLayout emptyLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_management);

        Log.d(TAG, "onCreate: Initializing Booking Management");
        
        requestQueue = Volley.newRequestQueue(this);
        initializeUI();
        fetchTodayBookings();
    }

    private void initializeUI() {
        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Views
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.textViewEmpty);
        emptyLayout = findViewById(R.id.layoutEmpty);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        findViewById(R.id.btnRetry).setOnClickListener(v -> fetchTodayBookings());
        swipeRefreshLayout.setOnRefreshListener(this::fetchTodayBookings);

        // Setup RecyclerView
        bookingList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BookingAdapter(this, bookingList, this::onBookingClicked);
        recyclerView.setAdapter(adapter);
        
        Log.d(TAG, "initializeUI: UI components initialized");
    }

    /**
     * Fetch all bookings for today
     */
    private void fetchTodayBookings() {
        Log.d(TAG, "fetchTodayBookings: Loading today's bookings");
        showLoading(true);
        
        String url = ApiConstants.BASE_URL + "get_today_bookings.php";
        Log.d(TAG, "API URL: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "API Response: " + response);
                    parseBookingsResponse(response);
                },
                error -> {
                    Log.e(TAG, "API Error: " + error.getMessage());
                    showLoading(false);
                    showEmpty(true);
                    Toast.makeText(BookingManagementActivity.this, R.string.failed_load_bookings, Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void parseBookingsResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean success = jsonResponse.optBoolean("success", false);
            
            if (!success) {
                Log.e(TAG, "API returned success=false");
                showEmpty(true);
                showLoading(false);
                return;
            }

            JSONArray bookingsArray = jsonResponse.optJSONArray("bookings");
            bookingList.clear();

            if (bookingsArray != null) {
                for (int i = 0; i < bookingsArray.length(); i++) {
                    JSONObject bookingObj = bookingsArray.getJSONObject(i);
                    
                    // Skip cancelled bookings
                    int status = bookingObj.optInt("status", 1);
                    if (status == 0 || status == 3) {
                        continue;
                    }
                    
                    Booking booking = new Booking(
                        bookingObj.optInt("bid"),
                        bookingObj.optInt("tid"),
                        bookingObj.optString("bkcname", "N/A"),
                        bookingObj.optString("bktel", "N/A"),
                        bookingObj.optString("bdate"),
                        bookingObj.optString("btime"),
                        bookingObj.optInt("pnum"),
                        status,
                        bookingObj.optString("purpose", ""),
                        bookingObj.optString("remark", ""),
                        bookingObj.optInt("capacity"),
                        (float) bookingObj.optDouble("x_position", 0),
                        (float) bookingObj.optDouble("y_position", 0)
                    );
                    bookingList.add(booking);
                }
            }

            adapter.notifyDataSetChanged();
            
            if (bookingList.isEmpty()) {
                showEmpty(true);
            } else {
                showEmpty(false);
            }
            showLoading(false);

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage());
            showEmpty(true);
            showLoading(false);
        }
    }

    private void onBookingClicked(Booking booking) {
        Log.d(TAG, "onBookingClicked: booking " + booking.getBid());
        showBookingDetailsDialog(booking);
    }

    private void showBookingDetailsDialog(Booking booking) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_booking_details, null);
        
        // Set booking details
        ((TextView) dialogView.findViewById(R.id.textViewTableNumber)).setText(
            getString(R.string.table_with_capacity, booking.getTid(), booking.getCapacity())
        );
        ((TextView) dialogView.findViewById(R.id.textViewCustomerName)).setText(booking.getBkcname());
        ((TextView) dialogView.findViewById(R.id.textViewCustomerPhone)).setText(booking.getBktel());
        ((TextView) dialogView.findViewById(R.id.textViewNumPeople)).setText(
            getString(R.string.person_count, booking.getPnum())
        );
        ((TextView) dialogView.findViewById(R.id.textViewBookingTime)).setText(booking.getBtime());
        ((TextView) dialogView.findViewById(R.id.textViewPurpose)).setText(
            booking.getPurpose() != null && !booking.getPurpose().isEmpty() 
                ? booking.getPurpose() 
                : getString(R.string.no_purpose_specified)
        );
        ((TextView) dialogView.findViewById(R.id.textViewRemark)).setText(
            booking.getRemark() != null && !booking.getRemark().isEmpty()
                ? booking.getRemark()
                : getString(R.string.no_remarks)
        );
        ((TextView) dialogView.findViewById(R.id.textViewStatus)).setText(booking.getStatusText());
        
        builder.setView(dialogView);

        // Buttons
        if (booking.getStatus() == 1) {
            // Pending - show Check-in button
            builder.setPositiveButton(R.string.check_in, (dialog, which) -> {
                performCheckin(booking);
            });
        } else if (booking.getStatus() == 2) {
            // Done/Checked-in - show Payment Processing button
            builder.setPositiveButton(R.string.process_payment, (dialog, which) -> {
                // Navigate to cash payment for this table
                Intent intent = new Intent(BookingManagementActivity.this, CashPaymentManagementActivity.class);
                intent.putExtra("TABLE_NUMBER", booking.getTid());
                startActivity(intent);
            });
        }
        
        builder.setNegativeButton(R.string.close, null);
        builder.show();
    }

    private void performCheckin(Booking booking) {
        Log.d(TAG, "performCheckin: booking " + booking.getBid());
        
        String url = ApiConstants.BASE_URL + "update_booking_checkin.php";
        
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("bid", booking.getBid());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    Log.d(TAG, "Check-in response: " + response.toString());
                    try {
                        boolean success = response.optBoolean("success", false);
                        if (success) {
                            Toast.makeText(BookingManagementActivity.this, 
                                R.string.customer_checked_in_successfully, Toast.LENGTH_SHORT).show();
                            fetchTodayBookings(); // Refresh list
                        } else {
                            String message = response.optString("message", getString(R.string.check_in_failed));
                            Toast.makeText(BookingManagementActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Check-in error: " + error.getMessage());
                    Toast.makeText(BookingManagementActivity.this,
                            getString(R.string.error_prefix, error.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean show) {
        emptyLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTodayBookings();
    }
}
