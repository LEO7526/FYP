package com.example.yummyrestaurant.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.example.yummyrestaurant.activities.StaffBaseActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.yummyrestaurant.activities.CreateMaterialActivity;
import com.example.yummyrestaurant.R; // 確保引用正確的 R

public class InventoryActivity extends StaffBaseActivity {

    private ViewPager2 viewPager;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ▼▼▼ 關鍵修改：Layout 名稱改為 activity_inventory_home ▼▼▼
        setContentView(R.layout.activity_inventory_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 設定標題
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.inventory_management);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 顯示返回箭頭
        }

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.ingredients);
                            break;
                        case 1:
                            tab.setText(R.string.recipes);
                            break;
                        case 2:
                            tab.setText(R.string.analysis);
                            break;
                        case 3:
                            tab.setText(R.string.consumption);
                            break;
                        case 4:
                            tab.setText(R.string.restock);
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

        if (item.getItemId() == R.id.action_add_material) {
            startActivity(new Intent(this, CreateMaterialActivity.class));
            return true;
        }

        if (item.getItemId() == R.id.action_refresh) {
            int currentTab = viewPager.getCurrentItem();
            if (adapter != null) {
                androidx.fragment.app.Fragment fragment = adapter.getFragmentAt(currentTab);
                if (fragment instanceof RefreshableTab) {
                    ((RefreshableTab) fragment).refreshData();
                    Toast.makeText(this, R.string.refreshing_current_tab, Toast.LENGTH_SHORT).show();
                }
            }
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