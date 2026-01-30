package com.example.yummyrestaurant.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

/**
 * BookingActivity - First step of booking process
 * 
 * Features:
 * - Date picker for booking date
 * - Time picker for booking time
 * - Number of people input
 * - Calls get_available_tables_layout.php API for full seating chart data
 * - Includes real-time occupancy status from table_orders
 * 
 * Author: YummyRestaurant
 * Version: 1.1 (Enhanced with Layout API)
 */
public class BookingActivity extends ThemeBaseActivity {

    private Button buttonSelectDate, buttonSelectTime, buttonFindTables;
    private EditText editTextNumberOfPeople;
    private int year, month, day, hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSelectTime = findViewById(R.id.buttonSelectTime);
        buttonFindTables = findViewById(R.id.buttonFindTables);
        editTextNumberOfPeople = findViewById(R.id.editTextNumberOfPeople);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        buttonSelectDate.setText(String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day));
        buttonSelectTime.setText(String.format(Locale.US, "%02d:%02d", hour, minute));

        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonSelectTime.setOnClickListener(v -> showTimePicker());
        buttonFindTables.setOnClickListener(v -> findTables());
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    year = selectedYear;
                    month = selectedMonth;
                    day = selectedDay;
                    buttonSelectDate.setText(String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day));
                }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    /**
     * Show time picker dialog
     */
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    hour = selectedHour;
                    minute = selectedMinute;
                    buttonSelectTime.setText(String.format(Locale.US, "%02d:%02d", hour, minute));
                }, hour, minute, true);
        timePickerDialog.show();
    }

    /**
     * Find available tables by calling the seating chart layout API
     */
    private void findTables() {
        String date = buttonSelectDate.getText().toString();
        String time = buttonSelectTime.getText().toString();
        String pnum = editTextNumberOfPeople.getText().toString();

        if (pnum.isEmpty()) {
            Toast.makeText(this, "Please enter a valid number of people.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int numPeople = Integer.parseInt(pnum);
            if (numPeople <= 0) {
                Toast.makeText(this, "Please enter a valid number of people.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch available tables in background
        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                String baseUrl = ApiConfig.getBaseUrl(this);
                // Call new get_available_tables_layout.php endpoint
                String urlString = String.format("%sget_available_tables_layout.php?date=%s&time=%s&pnum=%s",
                        baseUrl, date, time, pnum);
                
                Log.d("BookingActivity", "Calling API: " + urlString);
                
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000); // 10 seconds
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                Log.d("BookingActivity", "Response Code: " + responseCode);

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String apiResponse = response.toString();
                Log.d("BookingActivity", "API Response: " + apiResponse);

                // Parse JSON response
                try {
                    JSONObject jsonResponse = new JSONObject(apiResponse);
                    boolean success = jsonResponse.optBoolean("success", false);
                    
                    if (success) {
                        // Extract available tables array
                        String tablesJsonArray = jsonResponse.optJSONArray("tables").toString();
                        
                        if (tablesJsonArray.equals("[]") || jsonResponse.optInt("total_available", 0) == 0) {
                            runOnUiThread(() -> 
                                Toast.makeText(BookingActivity.this, 
                                    "No available tables found for the selected criteria.", 
                                    Toast.LENGTH_LONG).show()
                            );
                        } else {
                            // Proceed to confirm booking activity
                            runOnUiThread(() -> {
                                Intent intent = new Intent(BookingActivity.this, ConfirmBookingActivity.class);
                                intent.putExtra("AVAILABLE_TABLES_JSON", tablesJsonArray);
                                intent.putExtra("BOOKING_DATE", date);
                                intent.putExtra("BOOKING_TIME", time);
                                intent.putExtra("NUM_PEOPLE", pnum);
                                startActivity(intent);
                            });
                        }
                    } else {
                        String errorMessage = jsonResponse.optString("message", "Unknown error");
                        runOnUiThread(() -> 
                            Toast.makeText(BookingActivity.this, 
                                "Error: " + errorMessage, 
                                Toast.LENGTH_LONG).show()
                        );
                    }
                } catch (Exception e) {
                    Log.e("BookingActivity", "JSON parsing error: " + e.getMessage());
                    runOnUiThread(() -> 
                        Toast.makeText(BookingActivity.this, 
                            "Invalid response from server.", 
                            Toast.LENGTH_LONG).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("BookingActivity", "Network error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(BookingActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}

