package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConfig;
import com.example.yummyrestaurant.models.Table;
import com.example.yummyrestaurant.utils.RoleManager;
import com.example.yummyrestaurant.views.SeatingChartView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ConfirmBookingActivity - Second step of booking process
 * 
 * Features:
 * - Full-screen seating chart with interactive table selection
 * - Floating Action Button (FAB) at bottom-right
 * - Bottom sheet for entering booking details
 * - Real-time table occupancy status display
 * - Responsive design for all screen sizes
 * 
 * Architecture:
 * - SeatingChartView: Interactive table layout (main screen)
 * - FAB: Trigger to show bottom sheet (initially disabled)
 * - Bottom Sheet: Contains form for name, phone, purpose, remark
 * 
 * Author: YummyRestaurant
 * Version: 3.0 (Bottom Sheet + FAB Design)
 */
public class ConfirmBookingActivity extends AppCompatActivity implements SeatingChartView.OnTableSelectedListener {

    private SeatingChartView seatingChartView;
    private FloatingActionButton fabConfirm;
    private FrameLayout bottomSheetContainer;
    
    private List<Table> tableList = new ArrayList<>();
    private Table selectedTable;

    private String bookingDate, bookingTime, numPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_booking);

        // Initialize views
        seatingChartView = findViewById(R.id.seatingChartView);
        fabConfirm = findViewById(R.id.fabConfirm);
        bottomSheetContainer = findViewById(R.id.bottomSheetContainer);

        // Get booking parameters from intent
        Intent intent = getIntent();
        String tablesJson = intent.getStringExtra("AVAILABLE_TABLES_JSON");
        bookingDate = intent.getStringExtra("BOOKING_DATE");
        bookingTime = intent.getStringExtra("BOOKING_TIME");
        numPeople = intent.getStringExtra("NUM_PEOPLE");

        // Parse and setup seating chart
        parseTablesJson(tablesJson);
        setupSeatingChart();

        // Setup FAB click listener - shows bottom sheet when clicked
        fabConfirm.setOnClickListener(v -> showBookingDetailsSheet());

        // Restore selected table and form after rotation/configuration change
        if (savedInstanceState != null) {
            restoreStateAfterRotation(savedInstanceState);
        }
    }

    /**
     * Parse table data from JSON response
     * Supports new format with coordinates and real-time status
     */
    private void parseTablesJson(String json) {
        if (json == null || json.isEmpty()) {
            Toast.makeText(this, "No table data available.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tableObject = jsonArray.getJSONObject(i);
                
                int tid = tableObject.getInt("id");
                int capacity = tableObject.getInt("capacity");
                
                // Extract coordinate and status information
                float x = (float) tableObject.optDouble("x", 0);
                float y = (float) tableObject.optDouble("y", 0);
                String status = tableObject.optString("status", "available");
                boolean isAvailable = tableObject.optBoolean("is_available", true);
                boolean suitableForBooking = tableObject.optBoolean("suitable_for_booking", false);

                Table table = new Table(tid, capacity, x, y, status, isAvailable, suitableForBooking);
                tableList.add(table);
            }

            Log.d("ConfirmBooking", "Parsed " + tableList.size() + " tables");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ConfirmBooking", "Error parsing table JSON: " + e.getMessage());
            Toast.makeText(this, "Failed to parse table data.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Setup the seating chart view with tables and listener
     */
    private void setupSeatingChart() {
        if (tableList.isEmpty()) {
            Toast.makeText(this, "No tables available for this booking.", Toast.LENGTH_SHORT).show();
            return;
        }

        seatingChartView.setTables(tableList);
        seatingChartView.setOnTableSelectedListener(this);
    }

    /**
     * Save selected table and FAB state when configuration changes (rotation)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedTable != null) {
            outState.putInt("SELECTED_TABLE_ID", selectedTable.getTid());
            outState.putBoolean("FORM_VISIBLE", bottomSheetContainer.getChildCount() > 0);
        }
    }

    /**
     * Restore selected table and form display after rotation
     */
    private void restoreStateAfterRotation(Bundle savedInstanceState) {
        int selectedTableId = savedInstanceState.getInt("SELECTED_TABLE_ID", -1);
        boolean formWasVisible = savedInstanceState.getBoolean("FORM_VISIBLE", false);

        if (selectedTableId != -1) {
            // Find and restore selected table
            for (Table t : tableList) {
                if (t.getTid() == selectedTableId) {
                    selectedTable = t;
                    // Update visual state
                    seatingChartView.setSelectedTable(selectedTableId);
                    // Enable FAB
                    fabConfirm.setEnabled(true);
                    fabConfirm.setAlpha(1.0f);

                    // Re-show form if it was visible before rotation
                    if (formWasVisible) {
                        showBookingDetailsSheet();
                    }
                    break;
                }
            }
        }
    }

    /**
     * Callback when a table is selected on the seating chart
     */
    @Override
    public void onTableSelected(Table table) {
        this.selectedTable = table;
        Log.d("ConfirmBooking", "Table selected: " + table.getTid() + " (Capacity: " + table.getCapacity() + ")");
        
        // Enable FAB when a table is selected
        fabConfirm.setEnabled(true);
        fabConfirm.setAlpha(1.0f);
        
        Toast.makeText(this, "Table " + table.getTid() + " selected. Tap confirm to continue.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback when an unavailable table is clicked
     */
    @Override
    public void onTableUnavailable(int tableId) {
        // Find the table to provide detailed feedback
        Table table = null;
        for (Table t : tableList) {
            if (t.getTid() == tableId) {
                table = t;
                break;
            }
        }

        String message;
        if (table != null) {
            // Check why table is unavailable
            if ("occupied".equals(table.getStatus())) {
                message = "Table " + tableId + " is occupied.";
            } else if ("reserved".equals(table.getStatus())) {
                message = "Table " + tableId + " is already reserved.";
            } else if (!table.isSuitableForBooking()) {
                // Table is available but too small for the party size
                message = "Table " + tableId + " (capacity " + table.getCapacity() + ") is too small for " + numPeople + " people.";
            } else {
                message = "Table " + tableId + " is not available.";
            }
        } else {
            message = "Table " + tableId + " is not available.";
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show the bottom sheet for booking details
     */
    private void showBookingDetailsSheet() {
        if (selectedTable == null) {
            Toast.makeText(this, "Please select a table first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate bottom sheet layout
        FrameLayout sheetView = new FrameLayout(this);
        bottomSheetContainer.removeAllViews();
        bottomSheetContainer.addView(sheetView);

        // Inflate the booking details sheet layout
        getLayoutInflater().inflate(R.layout.booking_details_sheet, bottomSheetContainer);

        // Get references to sheet controls
        TextView textViewSelectedTable = findViewById(R.id.textViewSelectedTable);
        EditText editTextName = findViewById(R.id.editTextBookingName);
        EditText editTextPhone = findViewById(R.id.editTextBookingPhone);
        EditText editTextPurpose = findViewById(R.id.editTextBookingPurpose);
        EditText editTextRemark = findViewById(R.id.editTextBookingRemark);
        Button buttonCancel = findViewById(R.id.buttonCancel);
        Button buttonConfirmBooking = findViewById(R.id.buttonConfirmBooking);

        // Display selected table info
        textViewSelectedTable.setText("Table #" + selectedTable.getTid() + " (Capacity: " + selectedTable.getCapacity() + ")");

        // Pre-fill user information from RoleManager
        String userName = RoleManager.getUserName();
        String userPhone = RoleManager.getUserTel();
        if (userName != null && !userName.isEmpty()) {
            editTextName.setText(userName);
        }
        if (userPhone != null && !userPhone.isEmpty()) {
            editTextPhone.setText(userPhone);
        }

        // Cancel button - close bottom sheet
        buttonCancel.setOnClickListener(v -> {
            selectedTable = null;
            fabConfirm.setEnabled(false);
            fabConfirm.setAlpha(0.5f);
            bottomSheetContainer.removeAllViews();
            Toast.makeText(this, "Booking cancelled. Select another table.", Toast.LENGTH_SHORT).show();
        });

        // Confirm button - submit booking
        buttonConfirmBooking.setOnClickListener(v -> submitBooking(
            editTextName.getText().toString().trim(),
            editTextPhone.getText().toString().trim(),
            editTextPurpose.getText().toString().trim(),
            editTextRemark.getText().toString().trim()
        ));
    }

    /**
     * Submit booking to server
     */
    private void submitBooking(String name, String phone, String purpose, String remark) {
        if (selectedTable == null) {
            Toast.makeText(this, "Please select a table.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please enter your name and phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send booking request in background thread
        new Thread(() -> {
            HttpURLConnection conn = null;
            OutputStream os = null;
            try {
                String baseUrl = ApiConfig.getBaseUrl(this);
                String urlString = baseUrl + "create_booking.php";
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                // Build booking JSON
                JSONObject bookingJson = new JSONObject();
                bookingJson.put("bkcname", name);
                bookingJson.put("bktel", phone);
                bookingJson.put("tid", selectedTable.getTid());
                bookingJson.put("bdate", bookingDate);
                bookingJson.put("btime", bookingTime);
                try {
                    bookingJson.put("pnum", Integer.parseInt(numPeople));
                } catch (NumberFormatException e) {
                    runOnUiThread(() -> Toast.makeText(ConfirmBookingActivity.this, "Invalid number of people.", Toast.LENGTH_LONG).show());
                    return;
                }
                bookingJson.put("purpose", purpose);
                bookingJson.put("remark", remark);

                // Send POST request
                os = conn.getOutputStream();
                byte[] input = bookingJson.toString().getBytes("utf-8");
                os.write(input, 0, input.length);

                int responseCode = conn.getResponseCode();

                runOnUiThread(() -> {
                    if (responseCode == 201) {
                        Toast.makeText(ConfirmBookingActivity.this, "Booking successful!", Toast.LENGTH_LONG).show();
                        finish(); // Close the booking activity
                    } else {
                        Toast.makeText(ConfirmBookingActivity.this, "Booking failed. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ConfirmBooking", "Booking error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ConfirmBookingActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                try {
                    if (os != null) os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
