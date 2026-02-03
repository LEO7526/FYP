package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;

public class CreateMaterialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_material);

        Button btnSave = findViewById(R.id.btnSaveMaterial);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 之後寫上傳 PHP 邏輯
                Toast.makeText(CreateMaterialActivity.this, "Material Added!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}