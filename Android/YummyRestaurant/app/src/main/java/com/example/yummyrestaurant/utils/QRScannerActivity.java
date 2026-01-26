
package com.example.yummyrestaurant.utils;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.activities.BrowseMenuActivity;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

public class QRScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final String TAG = "QRScannerActivity";
    private BarcodeView barcodeView;
    private boolean scanCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_view);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startScanner();
        }
    }

    private void startScanner() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null && !scanCompleted) {
                    scanCompleted = true;
                    barcodeView.pause(); // Stop scanning after first result
                    
                    String qrContent = result.getText();
                    Log.d(TAG, "QR Code scanned: " + qrContent);
                    
                    // Parse table ID from QR code
                    int tableId = TableManager.parseTableIdFromQRCode(qrContent);
                    
                    if (tableId > 0) {
                        Log.d(TAG, "Extracted table ID: " + tableId);
                        // Verify the table with backend
                        verifyTableAndProceed(tableId);
                    } else {
                        Log.w(TAG, "Failed to parse table ID from QR code");
                        showError("Invalid QR Code", "Could not extract table information from QR code.");
                    }
                }
            }
        });
        barcodeView.resume();
    }

    /**
     * Verify table ID with backend and proceed if valid
     */
    private void verifyTableAndProceed(int tableId) {
        TableManager.verifyTable(QRScannerActivity.this, tableId, new TableManager.TableVerificationCallback() {
            @Override
            public void onVerificationSuccess(TableManager.TableVerificationResponse response) {
                Log.d(TAG, "Table verification successful. Table: " + response.table_id + 
                        ", Status: " + response.status + ", Available: " + response.available);
                
                if (response.available) {
                    // Table is available, set in CartManager and proceed to menu
                    CartManager.setOrderType("dine_in");
                    CartManager.setTableNumber(response.table_id);
                    
                    Toast.makeText(QRScannerActivity.this, 
                            "Table " + response.table_id + " confirmed! (Capacity: " + 
                            response.capacity + " seats)", 
                            Toast.LENGTH_SHORT).show();
                    
                    Log.d(TAG, "Navigating to BrowseMenuActivity with table: " + response.table_id);
                    
                    // Navigate to menu with table info
                    Intent intent = new Intent(QRScannerActivity.this, BrowseMenuActivity.class);
                    intent.putExtra("table_number", response.table_id);
                    intent.putExtra("table_capacity", response.capacity);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    // Table is not available
                    showError("Table Not Available", 
                            "Table " + response.table_id + " is not available (" + 
                            response.status + "). Please select another table.");
                }
            }

            @Override
            public void onVerificationFailure(String errorMessage) {
                Log.e(TAG, "Table verification failed: " + errorMessage);
                showError("Verification Failed", errorMessage);
            }
        });
    }

    /**
     * Show error dialog and allow user to retry scanning
     */
    private void showError(String title, String message) {
        new AlertDialog.Builder(QRScannerActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> {
                    scanCompleted = false;
                    barcodeView.resume();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}