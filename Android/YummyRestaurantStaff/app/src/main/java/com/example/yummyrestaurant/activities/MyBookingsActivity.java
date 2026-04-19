package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.MyBookingAdapter;
import com.example.yummyrestaurant.api.ApiConfig;
import com.example.yummyrestaurant.models.MyBooking;
import com.example.yummyrestaurant.utils.RoleManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends ThemeBaseActivity {

    private RecyclerView recyclerMyBookings;
    private TextView textEmptyBookings;
    private ProgressBar progressMyBookings;

    private MyBookingAdapter adapter;
    private final List<MyBooking> bookingList = new ArrayList<>();
    private int currentCid = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ensureCustomerLoggedIn()) {
            return;
        }

        setContentView(R.layout.activity_my_bookings);

        recyclerMyBookings = findViewById(R.id.recyclerMyBookings);
        textEmptyBookings = findViewById(R.id.textEmptyBookings);
        progressMyBookings = findViewById(R.id.progressMyBookings);

        recyclerMyBookings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyBookingAdapter(bookingList, this::confirmCancelBooking);
        recyclerMyBookings.setAdapter(adapter);

        loadMyBookings();
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
            Toast.makeText(this, getString(R.string.please_login_customer_first), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return false;
        }

        try {
            currentCid = Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.invalid_customer_account_login_again), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return false;
        }

        return true;
    }

    private void loadMyBookings() {
        progressMyBookings.setVisibility(View.VISIBLE);

        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                String baseUrl = ApiConfig.getBaseUrl(this);
                String urlString = baseUrl + "get_my_bookings.php?cid=" + URLEncoder.encode(String.valueOf(currentCid), "UTF-8");

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONObject jsonObject = new JSONObject(response.toString());
                boolean success = jsonObject.optBoolean("success", false);

                if (!success) {
                    String message = jsonObject.optString("message", getString(R.string.failed_load_bookings));
                    runOnUiThread(() -> {
                        progressMyBookings.setVisibility(View.GONE);
                        Toast.makeText(MyBookingsActivity.this, message, Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                JSONArray data = jsonObject.optJSONArray("data");
                List<MyBooking> tempList = new ArrayList<>();

                if (data != null) {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject item = data.getJSONObject(i);
                        tempList.add(new MyBooking(
                                item.optInt("bid"),
                                item.optInt("cid"),
                                item.optString("bkcname", ""),
                                item.optString("bktel", ""),
                                item.optInt("tid"),
                                item.optString("bdate", ""),
                                item.optString("btime", ""),
                                item.optInt("pnum"),
                                item.optString("purpose", ""),
                                item.optString("remark", ""),
                                item.optInt("status", 1)
                        ));
                    }
                }

                runOnUiThread(() -> {
                    progressMyBookings.setVisibility(View.GONE);
                    bookingList.clear();
                    bookingList.addAll(tempList);
                    adapter.notifyDataSetChanged();
                    textEmptyBookings.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
                });

            } catch (Exception e) {
                Log.e("MyBookingsActivity", "loadMyBookings error", e);
                runOnUiThread(() -> {
                    progressMyBookings.setVisibility(View.GONE);
                    Toast.makeText(MyBookingsActivity.this, getString(R.string.error_with_reason, e.getMessage()), Toast.LENGTH_LONG).show();
                });
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (Exception ignored) {
                }
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void confirmCancelBooking(MyBooking booking) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cancel_booking_title))
                .setMessage(getString(R.string.confirm_cancel_booking, booking.getBid()))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> cancelBooking(booking))
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void cancelBooking(MyBooking booking) {
        progressMyBookings.setVisibility(View.VISIBLE);

        new Thread(() -> {
            HttpURLConnection conn = null;
            OutputStream os = null;
            BufferedReader reader = null;
            try {
                String baseUrl = ApiConfig.getBaseUrl(this);
                String urlString = baseUrl + "cancel_booking.php";
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject payload = new JSONObject();
                payload.put("bid", booking.getBid());
                payload.put("cid", currentCid);

                os = conn.getOutputStream();
                os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();

                int responseCode = conn.getResponseCode();

                BufferedReader tempReader = new BufferedReader(new InputStreamReader(
                        responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                        StandardCharsets.UTF_8));
                reader = tempReader;

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONObject jsonObject = new JSONObject(response.toString());
                boolean success = jsonObject.optBoolean("success", false);
                String message = jsonObject.optString("message", success ? getString(R.string.booking_cancelled) : getString(R.string.failed_cancel_booking));

                runOnUiThread(() -> {
                    progressMyBookings.setVisibility(View.GONE);
                    Toast.makeText(MyBookingsActivity.this, message, Toast.LENGTH_LONG).show();
                    if (success) {
                        loadMyBookings();
                    }
                });

            } catch (Exception e) {
                Log.e("MyBookingsActivity", "cancelBooking error", e);
                runOnUiThread(() -> {
                    progressMyBookings.setVisibility(View.GONE);
                    Toast.makeText(MyBookingsActivity.this, getString(R.string.error_with_reason, e.getMessage()), Toast.LENGTH_LONG).show();
                });
            } finally {
                try {
                    if (os != null) os.close();
                } catch (Exception ignored) {
                }
                try {
                    if (reader != null) reader.close();
                } catch (Exception ignored) {
                }
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
