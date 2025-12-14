package com.example.yummyrestaurant.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

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

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    hour = selectedHour;
                    minute = selectedMinute;
                    buttonSelectTime.setText(String.format(Locale.US, "%02d:%02d", hour, minute));
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void findTables() {
        String date = buttonSelectDate.getText().toString();
        String time = buttonSelectTime.getText().toString();
        String pnum = editTextNumberOfPeople.getText().toString();

        if (pnum.isEmpty() || Integer.parseInt(pnum) <= 0) {
            Toast.makeText(this, "Please enter a valid number of people.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String baseUrl = ApiConfig.getBaseUrl(this);
                String urlString = String.format("%sget_available_tables.php?date=%s&time=%s&pnum=%s",
                        baseUrl, date, time, pnum);
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                String availableTablesJson = response.toString();
                Log.d("BookingActivity", "API Response: " + availableTablesJson);

                runOnUiThread(() -> {
                    if (availableTablesJson.equals("[]")) {
                        Toast.makeText(BookingActivity.this, "No available tables found for the selected criteria.", Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(BookingActivity.this, ConfirmBookingActivity.class);
                        intent.putExtra("AVAILABLE_TABLES_JSON", availableTablesJson);
                        intent.putExtra("BOOKING_DATE", date);
                        intent.putExtra("BOOKING_TIME", time);
                        intent.putExtra("NUM_PEOPLE", pnum);
                        startActivity(intent);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(BookingActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
