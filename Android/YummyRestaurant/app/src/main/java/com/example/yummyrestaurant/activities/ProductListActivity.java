package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.model.Product; // 导入 Product 类
import com.example.yummyrestaurant.adapters.ProductAdapter; // 假设你的适配器在 adapters 包中
import java.util.List;

public class ProductListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        recyclerView = findViewById(R.id.recyclerView);
        productList = Product.getSampleProducts(); // 获取示例产品列表
        recyclerView.setAdapter(new ProductAdapter(productList)); // 设置适配器
    }
}
