package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.TableAdapter;
import com.example.yummyrestaurant.api.ApiConfig;
import com.example.yummyrestaurant.models.Table;
import com.example.yummyrestaurant.utils.RoleManager;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConfirmBookingActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTables;
    private TableAdapter tableAdapter;
    private List<Table> tableList = new ArrayList<>();
    private EditText editTextName, editTextPhone, editTextPurpose, editTextRemark;
    private Button buttonConfirmBooking;

    private String bookingDate, bookingTime, numPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_booking);

        recyclerViewTables = findViewById(R.id.recyclerViewTables);
        editTextName = findViewById(R.id.editTextBookingName);
        editTextPhone = findViewById(R.id.editTextBookingPhone);
        editTextPurpose = findViewById(R.id.editTextBookingPurpose);
        editTextRemark = findViewById(R.id.editTextBookingRemark);
        buttonConfirmBooking = findViewById(R.id.buttonConfirmBooking);

        Intent intent = getIntent();
        String tablesJson = intent.getStringExtra("AVAILABLE_TABLES_JSON");
        bookingDate = intent.getStringExtra("BOOKING_DATE");
        bookingTime = intent.getStringExtra("BOOKING_TIME");
        numPeople = intent.getStringExtra("NUM_PEOPLE");

        // Pre-fill user information from RoleManager
        String userName = RoleManager.getUserName();
        String userPhone = RoleManager.getUserTel();
        if (userName != null && !userName.isEmpty()) {
            editTextName.setText(userName);
        }
        if (userPhone != null && !userPhone.isEmpty()) {
            editTextPhone.setText(userPhone);
        }

        parseTablesJson(tablesJson);
        setupRecyclerView();

        buttonConfirmBooking.setOnClickListener(v -> submitBooking());
    }

    private void parseTablesJson(String json) {
        if (json == null || json.isEmpty()) return;
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tableObject = jsonArray.getJSONObject(i);
                int tid = tableObject.getInt("tid");
                int capacity = tableObject.getInt("capacity");
                tableList.add(new Table(tid, capacity));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse table data.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        tableAdapter = new TableAdapter(tableList, table -> {
            Log.d("ConfirmBooking", "Table selected: " + table.getTid());
        });
        recyclerViewTables.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTables.setAdapter(tableAdapter);
    }

    private void submitBooking() {
        Table selectedTable = tableAdapter.getSelectedTable();
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String purpose = editTextPurpose.getText().toString().trim();
        String remark = editTextRemark.getText().toString().trim();

        if (selectedTable == null) {
            Toast.makeText(this, "Please select a table.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please enter your name and phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

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
