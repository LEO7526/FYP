package com.example.yummyrestaurant.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView; // 👈 關鍵！一定要有這一行
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.DishListAdapter;
import com.example.yummyrestaurant.api.ApiConstants;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CreateDishActivity extends AppCompatActivity {

    // Tabs & Lists
    private TabLayout tabMain;
    private View viewCreate;
    private RecyclerView rvDishList;
    private DishListAdapter listAdapter;
    private List<JSONObject> dishList = new ArrayList<>();

    // Create Form UI
    private EditText etPrice;
    private Spinner spCategory, spSpice;
    private CheckBox cbAvailable;
    private EditText etNameEn, etDescEn, etNameTw, etDescTw, etNameCn, etDescCn;
    private LinearLayout llRecipeContainer;
    private ImageView ivPreview;
    private LinearLayout llSelectImagePrompt;

    // Data
    private String encodedImage = "";
    private List<CategoryItem> categoryList = new ArrayList<>();
    private List<MaterialItem> materialList = new ArrayList<>();

    // Inner Classes
    class CategoryItem {
        int id; String name;
        public CategoryItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
    class MaterialItem {
        int id; String name; String unit;
        public MaterialItem(int id, String name, String unit) { this.id = id; this.name = name; this.unit = unit; }
        @Override public String toString() { return name; }
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        Uri imageUri = result.getData().getData();
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ivPreview.setImageBitmap(bitmap);
                        ivPreview.setVisibility(View.VISIBLE);
                        llSelectImagePrompt.setVisibility(View.GONE);
                        encodedImage = bitmapToBase64(bitmap);
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dish);

        initViews();
        setupTabs();
        fetchMetadata();
    }

    private void initViews() {
        tabMain = findViewById(R.id.tabMain);
        viewCreate = findViewById(R.id.viewCreate);
        rvDishList = findViewById(R.id.rvDishList);

        // 初始化 Adapter，傳入點擊事件監聽器
        rvDishList.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new DishListAdapter(this, dishList, this::showDishDetails); // 點擊時呼叫 showDishDetails
        rvDishList.setAdapter(listAdapter);

        etPrice = findViewById(R.id.etPrice);
        spCategory = findViewById(R.id.spCategory);
        spSpice = findViewById(R.id.spSpice);
        cbAvailable = findViewById(R.id.cbAvailable);
        etNameEn = findViewById(R.id.etNameEn); etDescEn = findViewById(R.id.etDescEn);
        etNameTw = findViewById(R.id.etNameTw); etDescTw = findViewById(R.id.etDescTw);
        etNameCn = findViewById(R.id.etNameCn); etDescCn = findViewById(R.id.etDescCn);
        llRecipeContainer = findViewById(R.id.llRecipeContainer);
        ivPreview = findViewById(R.id.ivPreview);
        llSelectImagePrompt = findViewById(R.id.llSelectImagePrompt);

        findViewById(R.id.btnSelectImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
        findViewById(R.id.btnAddMaterial).setOnClickListener(v -> addMaterialRow());
        findViewById(R.id.btnSaveDish).setOnClickListener(v -> submitDish());

        String[] spices = {"0 - No Spice", "1 - Mild", "2 - Medium", "3 - Hot", "4 - Very Hot", "5 - Extreme"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spices);
        spSpice.setAdapter(adapter);
    }

    // === 顯示詳細資料彈窗 (重點功能) ===
    private void showDishDetails(int dishId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_dish_detail, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // 綁定 UI
        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvPrice = view.findViewById(R.id.tvDetailPrice);
        TextView tvCategory = view.findViewById(R.id.tvDetailCategory);
        TextView tvSpice = view.findViewById(R.id.tvDetailSpice);
        TextView tvRecipe = view.findViewById(R.id.tvDetailRecipe);
        TextView tvDesc = view.findViewById(R.id.tvDetailDesc);
        ImageView ivImage = view.findViewById(R.id.ivDetailImage);
        Button btnClose = view.findViewById(R.id.btnCloseDetail);

        tvName.setText("Loading...");
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        // 呼叫 API
        String url = ApiConstants.BASE_URL + "get_dish_detail.php?item_id=" + dishId;
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            JSONObject data = json.getJSONObject("data");
                            JSONObject info = data.getJSONObject("info");
                            JSONObject names = data.getJSONObject("names");
                            JSONArray recipe = data.getJSONArray("recipe");

                            // 顯示基本資料 (優先顯示英文，沒有則顯示 Walking)
                            String enName = names.has("en") ? names.getJSONObject("en").getString("name") : "No Name";
                            String enDesc = names.has("en") ? names.getJSONObject("en").getString("desc") : "";

                            tvName.setText(enName);
                            tvDesc.setText(enDesc);
                            tvPrice.setText("$" + info.getString("item_price"));
                            tvCategory.setText("Category: " + info.getString("category_name"));
                            tvSpice.setText("Spice Level: " + info.getString("spice_level"));

                            // 顯示食譜
                            if (recipe.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < recipe.length(); i++) {
                                    sb.append("• ").append(recipe.getString(i)).append("\n");
                                }
                                tvRecipe.setText(sb.toString());
                            } else {
                                tvRecipe.setText("No ingredients listed.");
                            }

                            // 載入圖片 (使用簡單的 Thread 下載)
                            String imgUrl = info.getString("image_url");
                            if (!imgUrl.isEmpty()) {
                                new Thread(() -> {
                                    try {
                                        Bitmap bmp = BitmapFactory.decodeStream(new URL(imgUrl).openStream());
                                        runOnUiThread(() -> ivImage.setImageBitmap(bmp));
                                    } catch (Exception e) { e.printStackTrace(); }
                                }).start();
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(this, "Error loading details", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void setupTabs() {
        tabMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    viewCreate.setVisibility(View.VISIBLE);
                    rvDishList.setVisibility(View.GONE);
                } else {
                    viewCreate.setVisibility(View.GONE);
                    rvDishList.setVisibility(View.VISIBLE);
                    fetchDishList();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchDishList() {
        String url = ApiConstants.BASE_URL + "get_dish_list.php";
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            JSONArray arr = json.getJSONArray("data");
                            dishList.clear();
                            for(int i=0; i<arr.length(); i++) dishList.add(arr.getJSONObject(i));
                            listAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {}
                }, error -> {}
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void submitDish() {
        if (etPrice.getText().toString().isEmpty() || etNameEn.getText().toString().isEmpty()) return;

        try {
            JSONObject json = new JSONObject();
            CategoryItem selectedCat = (CategoryItem) spCategory.getSelectedItem();
            json.put("category_id", selectedCat != null ? selectedCat.id : 1);
            json.put("price", etPrice.getText().toString());
            json.put("image_data", encodedImage);
            json.put("spice_level", spSpice.getSelectedItemPosition());
            json.put("is_available", cbAvailable.isChecked());

            JSONObject transObj = new JSONObject();
            transObj.put("en", new JSONObject().put("name", etNameEn.getText()).put("description", etDescEn.getText()));
            transObj.put("zh-TW", new JSONObject().put("name", etNameTw.getText()).put("description", etDescTw.getText()));
            transObj.put("zh-CN", new JSONObject().put("name", etNameCn.getText()).put("description", etDescCn.getText()));
            json.put("translations", transObj);

            JSONArray recipeArr = new JSONArray();
            for (int i=0; i<llRecipeContainer.getChildCount(); i++) {
                View row = llRecipeContainer.getChildAt(i);
                Spinner sp = row.findViewById(R.id.spMaterial);
                EditText etQty = row.findViewById(R.id.etQty);
                MaterialItem m = (MaterialItem) sp.getSelectedItem();
                String qtyStr = etQty.getText().toString();
                if (m != null && m.id != 0 && !qtyStr.isEmpty()) {
                    recipeArr.put(new JSONObject().put("mid", m.id).put("quantity", qtyStr));
                }
            }
            json.put("recipe", recipeArr);

            String url = ApiConstants.BASE_URL + "create_dish.php";
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        Toast.makeText(this, "Dish Created!", Toast.LENGTH_SHORT).show();
                        tabMain.getTabAt(1).select();
                    },
                    error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );
            Volley.newRequestQueue(this).add(req);
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void fetchMetadata() {
        String url = ApiConstants.BASE_URL + "get_dish_metadata.php";
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONObject data = json.getJSONObject("data");

                        JSONArray cats = data.getJSONArray("categories");
                        categoryList.clear();
                        for (int i=0; i<cats.length(); i++) {
                            JSONObject c = cats.getJSONObject(i);
                            categoryList.add(new CategoryItem(c.getInt("category_id"), c.getString("category_name")));
                        }
                        spCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList));

                        JSONArray mats = data.getJSONArray("materials");
                        materialList.clear();
                        materialList.add(new MaterialItem(0, "Select Material", ""));
                        for (int i=0; i<mats.length(); i++) {
                            JSONObject m = mats.getJSONObject(i);
                            materialList.add(new MaterialItem(m.getInt("mid"), m.getString("mname"), m.getString("unit")));
                        }
                    } catch (Exception e) {}
                }, error -> {}
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void addMaterialRow() {
        View row = getLayoutInflater().inflate(R.layout.item_recipe_material, null);
        Spinner sp = row.findViewById(R.id.spMaterial);
        TextView tvUnit = row.findViewById(R.id.tvUnit);
        Button btnDel = row.findViewById(R.id.btnRemove);

        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, materialList));
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                tvUnit.setText(((MaterialItem)p.getSelectedItem()).unit);
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });
        btnDel.setOnClickListener(v -> llRecipeContainer.removeView(row));
        llRecipeContainer.addView(row);
    }
}