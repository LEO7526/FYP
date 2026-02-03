package com.example.yummyrestaurant.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.yummyrestaurant.R; // 確保引用正確的 R

public class InventoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ▼▼▼ 關鍵修改：Layout 名稱改為 activity_inventory_home ▼▼▼
        setContentView(R.layout.activity_inventory_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 設定標題
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Inventory Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 顯示返回箭頭
        }

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Materials");
                            break;
                        case 1:
                            tab.setText("Recipes");
                            break;
                        case 2:
                            tab.setText("Production");
                            break;
                    }
                }
        ).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ▼▼▼ 關鍵修改：Menu 名稱如果有改要對應 (例如 menu_inventory) ▼▼▼
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(InventoryActivity.this, HistoryActivity.class);
            startActivity(intent);
            return true;
        }
        // 處理返回箭頭點擊
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}