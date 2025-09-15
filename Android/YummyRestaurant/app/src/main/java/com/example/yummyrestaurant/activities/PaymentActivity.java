package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.PaymentApiService;
import com.example.yummyrestaurant.api.PaymentIntentResponse;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.android.paymentsheet.PaymentSheetResultCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity implements PaymentSheetResultCallback {

    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;

    private Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize Stripe with your publishable key
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51S56QLC1wirzkW6GoFfOawzrgqNOL5i1DxFatxz2Mr5OAMISZ84QFFkn16763PXc3uDPjpqsQxJLpzfV2q74ke6U00P2dWN9PO" // Replace with your actual Stripe publishable key
        );

        payButton = findViewById(R.id.payBtn);
        payButton.setEnabled(false); // Disable until client secret is fetched

        // Initialize PaymentSheet
        paymentSheet = new PaymentSheet(this, this);

        // Fetch PaymentIntent client secret from backend
        PaymentApiService service = RetrofitClient.getClient().create(PaymentApiService.class);
        Call<PaymentIntentResponse> call = service.createPaymentIntent();
        call.enqueue(new Callback<PaymentIntentResponse>() {
            @Override
            public void onResponse(Call<PaymentIntentResponse> call, Response<PaymentIntentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    paymentIntentClientSecret = response.body().getClientSecret();
                    payButton.setEnabled(true);
                } else {
                    Toast.makeText(PaymentActivity.this, "Failed to fetch payment intent", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentIntentResponse> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Handle payment button click
        payButton.setOnClickListener(v -> {
            if (paymentIntentClientSecret != null && !paymentIntentClientSecret.isEmpty()) {
                paymentSheet.presentWithPaymentIntent(
                        paymentIntentClientSecret,
                        new PaymentSheet.Configuration.Builder(getString(R.string.merchant_name))
                                .build()
                );
            } else {
                Toast.makeText(PaymentActivity.this, "Payment intent client secret is not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPaymentSheetResult(@NonNull PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment succeeded!", Toast.LENGTH_SHORT).show();
            // Handle post-payment success logic here
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            String error = ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage();
            Toast.makeText(this, "Payment failed: " + error, Toast.LENGTH_SHORT).show();
        }
    }
}