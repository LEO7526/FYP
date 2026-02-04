package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;

public class CreateMaterialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_material);

        Toast.makeText(this, "Create Material Activity - Under Development", Toast.LENGTH_SHORT).show();
    }
}