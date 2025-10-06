package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;

public class CustomizeDishActivity extends AppCompatActivity {

    public static final String EXTRA_SPICE_LEVEL = "spiceLevel";
    public static final String EXTRA_NOTES = "extraNotes";

    private Spinner spiceSpinner;
    private EditText notesEditText;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_dish);

        spiceSpinner = findViewById(R.id.spiceSpinner);
        notesEditText = findViewById(R.id.notesEditText);
        saveBtn = findViewById(R.id.saveCustomizationBtn);

        String[] spiceLevels = {"Mild", "Medium", "Hot", "Numbing"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, spiceLevels);
        spiceSpinner.setAdapter(adapter);

        saveBtn.setOnClickListener(v -> {
            String chosenSpiceLevel = spiceSpinner.getSelectedItem().toString();
            String notes = notesEditText.getText().toString();

            android.util.Log.d("CartDebug", "Customize save clicked: spice=" + chosenSpiceLevel + " notes=" + notes);

            Intent result = new Intent();
            result.putExtra(EXTRA_SPICE_LEVEL, chosenSpiceLevel);
            result.putExtra(EXTRA_NOTES, notes);
            setResult(RESULT_OK, result);
            finish();
        });
    }
}