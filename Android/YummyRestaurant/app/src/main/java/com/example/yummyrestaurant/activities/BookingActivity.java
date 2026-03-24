package com.example.yummyrestaurant.activities;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConfig;
import com.example.yummyrestaurant.utils.RoleManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

    private static final int OPEN_HOUR = 11;
    private static final int LAST_SLOT_HOUR = 21;
    private static final int LAST_SLOT_MINUTE = 0;
    private static final int ORDERING_CUTOFF_HOUR = 21;
    private static final int ORDERING_CUTOFF_MINUTE = 30;
    private static final int SLOT_INTERVAL_MINUTES = 30;
    private static final long MIN_ADVANCE_HOURS = 24;

    private Button buttonSelectDate, buttonSelectTime, buttonFindTables;
    private EditText editTextNumberOfPeople;
    private int year, month, day, hour, minute;
    private String[] timeSlotLabels;
    private String[] timeSlotValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ensureCustomerLoggedIn()) {
            return;
        }

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
        initTimeSlots();
        buttonSelectTime.setText(timeSlotValues[0]);

        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonSelectTime.setOnClickListener(v -> showTimePicker());
        buttonFindTables.setOnClickListener(v -> findTables());
    }

    private void initTimeSlots() {
        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();

        int currentHour = OPEN_HOUR;
        int currentMinute = 0;

        while (currentHour < LAST_SLOT_HOUR || (currentHour == LAST_SLOT_HOUR && currentMinute <= LAST_SLOT_MINUTE)) {
            String start = String.format(Locale.US, "%02d:%02d", currentHour, currentMinute);

            int endHour = currentHour;
            int endMinute = currentMinute + SLOT_INTERVAL_MINUTES;
            if (endMinute >= 60) {
                endHour += 1;
                endMinute -= 60;
            }

            String end = String.format(Locale.US, "%02d:%02d", endHour, endMinute);
            labels.add(start + " - " + end);
            values.add(start);

            currentMinute += SLOT_INTERVAL_MINUTES;
            if (currentMinute >= 60) {
                currentHour += 1;
                currentMinute -= 60;
            }
        }

        timeSlotLabels = labels.toArray(new String[0]);
        timeSlotValues = values.toArray(new String[0]);
    }

    private boolean ensureCustomerLoggedIn() {
        RoleManager.init(this);
        String userId = RoleManager.getUserId();
        String role = RoleManager.getUserRole();

        boolean isCustomerLoggedIn = userId != null
                && !userId.trim().isEmpty()
                && role != null
                && "customer".equalsIgnoreCase(role);

        if (!isCustomerLoggedIn) {
            Toast.makeText(this, getString(R.string.please_login_customer_before_booking), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return false;
        }

        return true;
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
        int checkedIndex = 0;
        String currentValue = buttonSelectTime.getText().toString();
        for (int index = 0; index < timeSlotValues.length; index++) {
            if (timeSlotValues[index].equals(currentValue)) {
                checkedIndex = index;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_time_slot))
                .setSingleChoiceItems(timeSlotLabels, checkedIndex, (dialog, which) -> {
                    buttonSelectTime.setText(timeSlotValues[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private boolean isValidBookingDateTime(String date, String time) {
        try {
            LocalDate bookingDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime bookingTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));

            LocalTime openTime = LocalTime.of(OPEN_HOUR, 0);
            LocalTime cutoffTime = LocalTime.of(ORDERING_CUTOFF_HOUR, ORDERING_CUTOFF_MINUTE);

            if (bookingTime.isBefore(openTime) || !bookingTime.isBefore(cutoffTime)) {
                return false;
            }

            if (!(bookingTime.getMinute() == 0 || bookingTime.getMinute() == 30)) {
                return false;
            }

            LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingTime);
            LocalDateTime now = LocalDateTime.now();
            long hoursAhead = Duration.between(now, bookingDateTime).toHours();

            return hoursAhead >= MIN_ADVANCE_HOURS;

        } catch (DateTimeParseException e) {
            Log.e("BookingActivity", "Invalid date/time format", e);
            return false;
        }
    }

    /**
     * Find available tables by calling the seating chart layout API
     */
    private void findTables() {
        String date = buttonSelectDate.getText().toString();
        String time = buttonSelectTime.getText().toString();
        String pnum = editTextNumberOfPeople.getText().toString();

        if (!isValidBookingDateTime(date, time)) {
            Toast.makeText(this,
                    getString(R.string.booking_time_invalid_rule),
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (pnum.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_valid_people_count), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int numPeople = Integer.parseInt(pnum);
            if (numPeople <= 0) {
                Toast.makeText(this, getString(R.string.please_enter_valid_people_count), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.please_enter_valid_number), Toast.LENGTH_SHORT).show();
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
                        int totalAvailable = jsonResponse.optInt("total_available", 0);

                        if (totalAvailable == 0) {
                            runOnUiThread(() -> 
                                Toast.makeText(BookingActivity.this, 
                                    getString(R.string.no_available_tables_for_criteria), 
                                    Toast.LENGTH_LONG).show()
                            );
                        } else {
                            // Send all tables to selection screen
                            // (available + occupied + reserved + too-small)
                            String tablesJsonArray = jsonResponse.optJSONArray("tables").toString();

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
                        String errorMessage = jsonResponse.optString("message", getString(R.string.unknown_error));
                        runOnUiThread(() -> 
                            Toast.makeText(BookingActivity.this, 
                                getString(R.string.error_with_reason, errorMessage), 
                                Toast.LENGTH_LONG).show()
                        );
                    }
                } catch (Exception e) {
                    Log.e("BookingActivity", "JSON parsing error: " + e.getMessage());
                    runOnUiThread(() -> 
                        Toast.makeText(BookingActivity.this, 
                            getString(R.string.invalid_server_response), 
                            Toast.LENGTH_LONG).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("BookingActivity", "Network error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(BookingActivity.this, getString(R.string.error_with_reason, e.getMessage()), Toast.LENGTH_LONG).show());
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

