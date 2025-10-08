package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Customization;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;

public class CustomizeDishActivity extends AppCompatActivity {

    public static final String EXTRA_MENU_ITEM = "menuItem";
    public static final String EXTRA_QUANTITY = "quantity";

    private Spinner spiceSpinner;
    private EditText notesEditText;
    private Button saveBtn;

    private MenuItem menuItem;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_dish);

        spiceSpinner = findViewById(R.id.spiceSpinner);
        notesEditText = findViewById(R.id.notesEditText);
        saveBtn = findViewById(R.id.saveCustomizationBtn);

        // Load extras
        menuItem = (MenuItem) getIntent().getSerializableExtra(EXTRA_MENU_ITEM);
        quantity = getIntent().getIntExtra(EXTRA_QUANTITY, 1);

        // Safety check
        if (menuItem == null) {
            Toast.makeText(this, "No dish data provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup spinner
        String[] spiceLevels = {"Mild", "Medium", "Hot", "Numbing"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, spiceLevels);
        spiceSpinner.setAdapter(adapter);

        saveBtn.setOnClickListener(v -> {
            String chosenSpiceLevel = spiceSpinner.getSelectedItem().toString();
            String notes = notesEditText.getText().toString();

            Customization customization = new Customization(chosenSpiceLevel, notes);
            CartItem cartItem = new CartItem(menuItem, customization);

            // Null-safe quantity update
            Integer qtyFromCart = CartManager.getItemQuantity(cartItem);
            int currentQty = (qtyFromCart != null) ? qtyFromCart : 0;

            CartManager.updateQuantity(cartItem, currentQty + quantity);

            // Confirmation toast
            String toastText = quantity + " × " + menuItem.getName();
            if (chosenSpiceLevel != null && !chosenSpiceLevel.isEmpty()) {
                toastText += " (" + chosenSpiceLevel + ")";
            }
            if (notes != null && !notes.isEmpty()) {
                toastText += " • " + notes;
            }

            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            finish();   // ✅ close Customize screen so you don’t stack it
        });
    }
}