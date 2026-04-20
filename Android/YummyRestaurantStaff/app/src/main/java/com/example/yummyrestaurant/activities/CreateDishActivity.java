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
import android.widget.AdapterView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.DishListAdapter;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.utils.LanguageManager;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CreateDishActivity extends StaffBaseActivity {

    // Tabs & Lists
    private TabLayout tabMain;
    private View viewCreate;
    private View viewPackageCreate;
    private RecyclerView rvDishList;
    private DishListAdapter listAdapter;
    private List<JSONObject> dishList = new ArrayList<>();

    // Create Form UI
    private EditText etPrice;
    private Spinner spCategory, spSpice;
    private CheckBox cbAvailable;
    private EditText etNameEn, etDescEn, etNameTw, etDescTw, etNameCn, etDescCn;
    private LinearLayout llRecipeContainer;
    private LinearLayout llPackageTypeContainer;
    private ImageView ivPreview;
    private ImageView ivPackagePreview;
    private LinearLayout llSelectImagePrompt;
    private LinearLayout llSelectPackageImagePrompt;
    private EditText etPackageName, etPackagePrice;

    // Data
    private String encodedImage = "";
    private String encodedPackageImage = "";
    private List<CategoryItem> categoryList = new ArrayList<>();
    private List<MaterialItem> materialList = new ArrayList<>();
    private List<DishOptionItem> packageDishOptions = new ArrayList<>();

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
    class DishOptionItem {
        int id; String name;
        public DishOptionItem(int id, String name) { this.id = id; this.name = name; }
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

    private final ActivityResultLauncher<Intent> packageImagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        Uri imageUri = result.getData().getData();
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ivPackagePreview.setImageBitmap(bitmap);
                        ivPackagePreview.setVisibility(View.VISIBLE);
                        llSelectPackageImagePrompt.setVisibility(View.GONE);
                        encodedPackageImage = bitmapToBase64(bitmap);
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
        fetchDishOptions();
    }

    private void initViews() {
        tabMain = findViewById(R.id.tabMain);
        viewCreate = findViewById(R.id.viewCreate);
        viewPackageCreate = findViewById(R.id.viewPackageCreate);
        rvDishList = findViewById(R.id.rvDishList);

        // 鍒濆鍖?Adapter锛屽偝鍏ラ粸鎿婁簨浠剁洠鑱藉櫒
        rvDishList.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new DishListAdapter(this, dishList, this::handleListItemClick);
        rvDishList.setAdapter(listAdapter);

        etPrice = findViewById(R.id.etPrice);
        spCategory = findViewById(R.id.spCategory);
        spSpice = findViewById(R.id.spSpice);
        cbAvailable = findViewById(R.id.cbAvailable);
        etNameEn = findViewById(R.id.etNameEn); etDescEn = findViewById(R.id.etDescEn);
        etNameTw = findViewById(R.id.etNameTw); etDescTw = findViewById(R.id.etDescTw);
        etNameCn = findViewById(R.id.etNameCn); etDescCn = findViewById(R.id.etDescCn);
        llRecipeContainer = findViewById(R.id.llRecipeContainer);
        llPackageTypeContainer = findViewById(R.id.llPackageTypeContainer);
        ivPreview = findViewById(R.id.ivPreview);
        ivPackagePreview = findViewById(R.id.ivPackagePreview);
        llSelectImagePrompt = findViewById(R.id.llSelectImagePrompt);
        llSelectPackageImagePrompt = findViewById(R.id.llSelectPackageImagePrompt);
        etPackageName = findViewById(R.id.etPackageName);
        etPackagePrice = findViewById(R.id.etPackagePrice);

        findViewById(R.id.btnSelectImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
        findViewById(R.id.btnSelectPackageImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            packageImagePickerLauncher.launch(intent);
        });
        findViewById(R.id.btnAddMaterial).setOnClickListener(v -> addMaterialRow());
        findViewById(R.id.btnSaveDish).setOnClickListener(v -> submitDish());
        findViewById(R.id.btnAddPackageType).setOnClickListener(v -> addPackageTypeRow());
        findViewById(R.id.btnSavePackage).setOnClickListener(v -> submitPackage());

        String[] spices = {"0 - No Spice", "1 - Mild", "2 - Medium", "3 - Hot", "4 - Very Hot", "5 - Extreme"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSpice.setAdapter(adapter);
    }

    // === 椤ず瑭崇窗璩囨枡褰堢獥 (閲嶉粸鍔熻兘) ===
    private void handleListItemClick(JSONObject item) {
        String itemType = item.optString("item_type", "dish");
        int id = item.optInt("id", 0);
        if ("package".equalsIgnoreCase(itemType)) {
            showPackageDetails(id);
        } else {
            showDishDetails(id);
        }
    }

    private void showDishDetails(int dishId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_dish_detail, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // 缍佸畾 UI
        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvPrice = view.findViewById(R.id.tvDetailPrice);
        TextView tvCategory = view.findViewById(R.id.tvDetailCategory);
        TextView tvSpice = view.findViewById(R.id.tvDetailSpice);
        TextView tvRecipe = view.findViewById(R.id.tvDetailRecipe);
        TextView tvDesc = view.findViewById(R.id.tvDetailDesc);
        ImageView ivImage = view.findViewById(R.id.ivDetailImage);
        Button btnClose = view.findViewById(R.id.btnCloseDetail);

        tvName.setText(R.string.loading);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        // 鍛煎彨 API
        String url = ApiConstants.baseUrl() + "get_dish_detail.php?item_id=" + dishId;
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            JSONObject data = json.getJSONObject("data");
                            JSONObject info = data.getJSONObject("info");
                            JSONObject names = data.getJSONObject("names");
                            JSONArray recipe = data.getJSONArray("recipe");

                            // 椤ず鍩烘湰璩囨枡 (鍎厛椤ず鑻辨枃锛屾矑鏈夊墖椤ず Walking)
                            String enName = names.has("en") ? names.getJSONObject("en").getString("name") : getString(R.string.no_name);
                            String enDesc = names.has("en") ? names.getJSONObject("en").getString("desc") : "";

                            tvName.setText(enName);
                            tvDesc.setText(enDesc);
                            tvPrice.setText(getString(R.string.price_label, info.getString("item_price")));
                            tvCategory.setText(getString(R.string.category_label, info.getString("category_name")));
                            tvSpice.setText(getString(R.string.spice_level_label, info.getString("spice_level")));

                            // 椤ず椋熻瓬
                            if (recipe.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < recipe.length(); i++) {
                                    sb.append("- ").append(recipe.getString(i)).append("\n");
                                }
                                tvRecipe.setText(sb.toString());
                            } else {
                                tvRecipe.setText(R.string.no_ingredients_listed);
                            }

                            // 杓夊叆鍦栫墖 (浣跨敤绨″柈鐨?Thread 涓嬭級)
                            String imgUrl = info.optString("image_url", "");
                            if (!imgUrl.isEmpty()) {
                                if (imgUrl.startsWith("data:image")) {
                                    try {
                                        int comma = imgUrl.indexOf(',');
                                        String payload = comma >= 0 ? imgUrl.substring(comma + 1) : imgUrl;
                                        byte[] bytes = Base64.decode(payload, Base64.DEFAULT);
                                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        if (bmp != null) {
                                            ivImage.setImageBitmap(bmp);
                                        }
                                    } catch (Exception ignored) {
                                    }
                                } else {
                                    String finalUrl = imgUrl;
                                    if (!imgUrl.toLowerCase(Locale.ROOT).startsWith("http")) {
                                        finalUrl = ApiConstants.baseUrl() + imgUrl;
                                    }
                                    String imageUrl = finalUrl;
                                    new Thread(() -> {
                                        try {
                                            Bitmap bmp = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
                                            runOnUiThread(() -> {
                                                if (bmp != null) ivImage.setImageBitmap(bmp);
                                            });
                                        } catch (Exception e) { e.printStackTrace(); }
                                    }).start();
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(this, R.string.error_loading_details, Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void showPackageDetails(int packageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_dish_detail, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvPrice = view.findViewById(R.id.tvDetailPrice);
        TextView tvCategory = view.findViewById(R.id.tvDetailCategory);
        TextView tvSpice = view.findViewById(R.id.tvDetailSpice);
        TextView tvRecipe = view.findViewById(R.id.tvDetailRecipe);
        TextView tvDesc = view.findViewById(R.id.tvDetailDesc);
        ImageView ivImage = view.findViewById(R.id.ivDetailImage);
        Button btnClose = view.findViewById(R.id.btnCloseDetail);

        tvName.setText(R.string.loading);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        String url = ApiConstants.baseUrl() + "get_package.php?id=" + packageId + "&lang=" + LanguageManager.getLangCode(this);
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.optBoolean("success", false)) {
                            Toast.makeText(this, R.string.failed_load_package_details, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONObject data = json.getJSONObject("data");
                        tvName.setText(data.optString("name", getString(R.string.package_default_name)));
                        tvPrice.setText(getString(R.string.price_label, data.optString("price", "0")));
                        tvCategory.setText(getString(R.string.type_dish_package));
                        tvSpice.setText(getString(R.string.sections_count, data.optInt("num_of_type", 0)));

                        JSONArray types = data.optJSONArray("types");
                        StringBuilder details = new StringBuilder();
                        if (types != null) {
                            for (int i = 0; i < types.length(); i++) {
                                JSONObject type = types.getJSONObject(i);
                                details.append("- ")
                                    .append(type.optString("name", getString(R.string.section_default_name)))
                                    .append(" (")
                                    .append(getString(R.string.choose_count, type.optInt("optional_quantity", 1)))
                                    .append(")")
                                    .append("\n");

                                JSONArray items = type.optJSONArray("items");
                                if (items != null) {
                                    for (int j = 0; j < items.length(); j++) {
                                        JSONObject packageDish = items.getJSONObject(j);
                                        details.append("   - ")
                                                .append(packageDish.optString("name", getString(R.string.dish_default_name)))
                                                .append("\n");
                                    }
                                }
                            }
                        }
                        tvRecipe.setText(details.length() == 0 ? getString(R.string.no_package_sections) : details.toString().trim());
                        tvDesc.setText(R.string.package_grouped_selectable_dishes);

                        String imgUrl = data.optString("image_url", "");
                        if (!imgUrl.isEmpty()) {
                            String finalUrl = imgUrl;
                            if (!imgUrl.toLowerCase(Locale.ROOT).startsWith("http") && !imgUrl.startsWith("data:image")) {
                                finalUrl = ApiConstants.baseUrl() + imgUrl;
                            }

                            if (imgUrl.startsWith("data:image")) {
                                int comma = imgUrl.indexOf(',');
                                String payload = comma >= 0 ? imgUrl.substring(comma + 1) : imgUrl;
                                byte[] bytes = Base64.decode(payload, Base64.DEFAULT);
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                if (bmp != null) {
                                    ivImage.setImageBitmap(bmp);
                                }
                            } else {
                                String imageUrl = finalUrl;
                                new Thread(() -> {
                                    try {
                                        Bitmap bmp = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
                                        runOnUiThread(() -> {
                                            if (bmp != null) ivImage.setImageBitmap(bmp);
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.failed_parse_package_details, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, R.string.error_loading_package_details, Toast.LENGTH_SHORT).show()
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
                    viewPackageCreate.setVisibility(View.GONE);
                } else if (tab.getPosition() == 1) {
                    viewCreate.setVisibility(View.GONE);
                    rvDishList.setVisibility(View.VISIBLE);
                    viewPackageCreate.setVisibility(View.GONE);
                    fetchDishList();
                } else {
                    viewCreate.setVisibility(View.GONE);
                    rvDishList.setVisibility(View.GONE);
                    viewPackageCreate.setVisibility(View.VISIBLE);
                    if (llPackageTypeContainer.getChildCount() == 0) {
                        addPackageTypeRow();
                    }
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchDishList() {
        List<JSONObject> combinedList = new ArrayList<>();
        String currentLang = LanguageManager.getLangCode(this);
        String dishUrl = ApiConstants.baseUrl() + "get_dish_list.php?lang=" + currentLang;
        String packageUrl = ApiConstants.baseUrl() + "get_packages.php?lang=" + currentLang;

        StringRequest dishReq = new StringRequest(Request.Method.GET, dishUrl,
                dishResponse -> {
                    try {
                        JSONObject json = new JSONObject(dishResponse);
                        if (json.optBoolean("success", false) || "true".equals(json.optString("success"))) {
                            JSONArray arr = json.getJSONArray("data");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject item = arr.getJSONObject(i);
                                item.put("item_type", "dish");
                                item.put("category", item.optString("category", getString(R.string.dish_default_name)));
                                combinedList.add(item);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("fetchDishList", "Dish parsing error", e);
                    }

                    StringRequest packageReq = new StringRequest(Request.Method.GET, packageUrl,
                            packageResponse -> {
                                try {
                                    JSONObject packageJson = new JSONObject(packageResponse);
                                    if (packageJson.optBoolean("success", false)) {
                                        JSONArray packageArr = packageJson.getJSONArray("data");
                                        for (int i = 0; i < packageArr.length(); i++) {
                                            JSONObject item = packageArr.getJSONObject(i);
                                            item.put("item_type", "package");
                                            item.put("category", getString(R.string.dish_package_label));
                                            combinedList.add(item);
                                        }
                                    }

                                    Collections.sort(combinedList, Comparator.comparing(o -> o.optString("name", ""), String.CASE_INSENSITIVE_ORDER));
                                    listAdapter.setData(combinedList);
                                } catch (Exception e) {
                                    android.util.Log.e("fetchDishList", "Package parsing error", e);
                                    listAdapter.setData(combinedList);
                                }
                            }, error -> {
                                android.util.Log.e("fetchDishList", "Package network error", error);
                                listAdapter.setData(combinedList);
                            });

                    Volley.newRequestQueue(this).add(packageReq);
                }, error -> android.util.Log.e("fetchDishList", "Dish network error", error)
        );
        Volley.newRequestQueue(this).add(dishReq);
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

            String url = ApiConstants.baseUrl() + "create_dish.php";
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        Toast.makeText(this, R.string.dish_created, Toast.LENGTH_SHORT).show();
                        tabMain.getTabAt(1).select();
                    },
                    error -> Toast.makeText(this,
                            getString(R.string.error_prefix, error.getMessage()),
                            Toast.LENGTH_SHORT).show()
            );
            Volley.newRequestQueue(this).add(req);
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void fetchDishOptions() {
        String url = ApiConstants.baseUrl() + "get_dish_list.php?lang=" + LanguageManager.getLangCode(this);
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.optBoolean("success", false)) {
                            return;
                        }
                        JSONArray arr = json.getJSONArray("data");
                        packageDishOptions.clear();
                        packageDishOptions.add(new DishOptionItem(0, getString(R.string.select_dish)));
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject item = arr.getJSONObject(i);
                            packageDishOptions.add(new DishOptionItem(item.getInt("id"), item.getString("name")));
                        }
                    } catch (Exception ignored) {
                    }
                }, error -> {
                }
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void addPackageTypeRow() {
        if (packageDishOptions == null || packageDishOptions.size() <= 1) {
            Toast.makeText(this, R.string.dish_list_not_ready, Toast.LENGTH_SHORT).show();
            fetchDishOptions();
            return;
        }

        View row = getLayoutInflater().inflate(R.layout.item_package_type_editor, llPackageTypeContainer, false);
        LinearLayout dishContainer = row.findViewById(R.id.llPackageDishContainer);
        Button btnAddDish = row.findViewById(R.id.btnAddPackageDish);
        Button btnRemoveType = row.findViewById(R.id.btnRemovePackageType);

        btnAddDish.setOnClickListener(v -> addPackageDishRow(dishContainer));
        btnRemoveType.setOnClickListener(v -> llPackageTypeContainer.removeView(row));

        llPackageTypeContainer.addView(row);
        addPackageDishRow(dishContainer);
    }

    private void addPackageDishRow(LinearLayout dishContainer) {
        View row = getLayoutInflater().inflate(R.layout.item_package_dish_option, dishContainer, false);
        Spinner spDish = row.findViewById(R.id.spPackageDish);
        Button btnRemove = row.findViewById(R.id.btnRemovePackageDish);

        ArrayAdapter<DishOptionItem> dishAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, packageDishOptions);
        dishAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDish.setAdapter(dishAdapter);
        if (packageDishOptions.size() > 1) {
            spDish.setSelection(1);
        }

        btnRemove.setOnClickListener(v -> dishContainer.removeView(row));
        dishContainer.addView(row);
    }

    private void submitPackage() {
        String packageName = etPackageName.getText().toString().trim();
        String priceValue = etPackagePrice.getText().toString().trim();

        if (packageName.isEmpty() || priceValue.isEmpty()) {
            Toast.makeText(this, R.string.package_name_price_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (llPackageTypeContainer.getChildCount() == 0) {
            Toast.makeText(this, R.string.add_at_least_one_package_section, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("package_name", packageName);
            json.put("price", Double.parseDouble(priceValue));
            json.put("image_data", encodedPackageImage);

            JSONArray typesArray = new JSONArray();
            for (int i = 0; i < llPackageTypeContainer.getChildCount(); i++) {
                View typeRow = llPackageTypeContainer.getChildAt(i);
                EditText etTypeNameEn = typeRow.findViewById(R.id.etTypeNameEn);
                EditText etTypeNameTw = typeRow.findViewById(R.id.etTypeNameTw);
                EditText etTypeNameCn = typeRow.findViewById(R.id.etTypeNameCn);
                EditText etOptionalQuantity = typeRow.findViewById(R.id.etOptionalQuantity);
                LinearLayout dishContainer = typeRow.findViewById(R.id.llPackageDishContainer);

                String typeNameEn = etTypeNameEn.getText().toString().trim();
                if (typeNameEn.isEmpty()) {
                    Toast.makeText(this, R.string.package_section_needs_english_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject typeObject = new JSONObject();
                JSONObject translations = new JSONObject();
                translations.put("en", typeNameEn);
                translations.put("zh-TW", etTypeNameTw.getText().toString().trim());
                translations.put("zh-CN", etTypeNameCn.getText().toString().trim());
                typeObject.put("translations", translations);
                typeObject.put("optional_quantity", etOptionalQuantity.getText().toString().trim().isEmpty() ? 1 : Integer.parseInt(etOptionalQuantity.getText().toString().trim()));

                JSONArray dishArray = new JSONArray();
                for (int j = 0; j < dishContainer.getChildCount(); j++) {
                    View dishRow = dishContainer.getChildAt(j);
                    Spinner spDish = dishRow.findViewById(R.id.spPackageDish);
                    EditText etPriceModifier = dishRow.findViewById(R.id.etPriceModifier);
                    DishOptionItem selectedDish = (DishOptionItem) spDish.getSelectedItem();
                    if (selectedDish != null && selectedDish.id > 0) {
                        JSONObject dishObject = new JSONObject();
                        dishObject.put("item_id", selectedDish.id);
                        dishObject.put("price_modifier", etPriceModifier.getText().toString().trim().isEmpty() ? 0 : Double.parseDouble(etPriceModifier.getText().toString().trim()));
                        dishArray.put(dishObject);
                    }
                }

                if (dishArray.length() == 0) {
                    Toast.makeText(this, R.string.package_section_needs_one_dish_option, Toast.LENGTH_SHORT).show();
                    return;
                }

                typeObject.put("dishes", dishArray);
                typesArray.put(typeObject);
            }

            json.put("types", typesArray);

            String url = ApiConstants.baseUrl() + "create_dish_package.php";
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        if (response.optBoolean("success", false)) {
                            Toast.makeText(this, R.string.dish_package_created, Toast.LENGTH_SHORT).show();
                            resetPackageForm();
                            tabMain.getTabAt(1).select();
                        } else {
                            Toast.makeText(this,
                                    response.optString("message", getString(R.string.failed_create_package)),
                                    Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this,
                            getString(R.string.error_prefix, error.getMessage()),
                            Toast.LENGTH_SHORT).show()
            );
            Volley.newRequestQueue(this).add(req);
        } catch (Exception e) {
            Toast.makeText(this, R.string.package_form_invalid, Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPackageForm() {
        etPackageName.setText("");
        etPackagePrice.setText("");
        encodedPackageImage = "";
        ivPackagePreview.setImageDrawable(null);
        ivPackagePreview.setVisibility(View.GONE);
        llSelectPackageImagePrompt.setVisibility(View.VISIBLE);
        llPackageTypeContainer.removeAllViews();
        addPackageTypeRow();
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void fetchMetadata() {
        String url = ApiConstants.baseUrl() + "get_dish_metadata.php";
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONObject data = json.optJSONObject("data");
                        if (data == null) {
                            Toast.makeText(this, R.string.metadata_format_invalid, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray cats = data.getJSONArray("categories");
                        categoryList.clear();
                        for (int i=0; i<cats.length(); i++) {
                            JSONObject c = cats.getJSONObject(i);
                            categoryList.add(new CategoryItem(c.getInt("category_id"), c.getString("category_name")));
                        }
                        spCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList));

                        JSONArray mats = data.getJSONArray("materials");
                        materialList.clear();
                        materialList.add(new MaterialItem(0, getString(R.string.select_ingredient), ""));
                        for (int i=0; i<mats.length(); i++) {
                            JSONObject m = mats.getJSONObject(i);
                            materialList.add(new MaterialItem(m.getInt("mid"), m.getString("mname"), m.getString("unit")));
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.metadata_parse_failed, Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(this, R.string.failed_load_categories_ingredients, Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void addMaterialRow() {
        if (materialList == null || materialList.size() <= 1) {
            Toast.makeText(this, R.string.ingredients_not_ready, Toast.LENGTH_SHORT).show();
            fetchMetadata();
            return;
        }

        View row = getLayoutInflater().inflate(R.layout.item_recipe_material, null);
        Spinner sp = row.findViewById(R.id.spMaterial);
        TextView tvUnit = row.findViewById(R.id.tvUnit);
        Button btnDel = row.findViewById(R.id.btnRemove);

        ArrayAdapter<MaterialItem> materialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, materialList);
        materialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(materialAdapter);
        if (materialList.size() > 1) {
            sp.setSelection(1);
            tvUnit.setText(materialList.get(1).unit);
        }
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

