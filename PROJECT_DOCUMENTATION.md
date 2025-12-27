# Yummy Restaurant - 完整項目文檔

**最後更新日期：2025年12月27日**

---

## 目錄
1. [數據庫結構](#數據庫結構)
2. [PHP API 端點](#php-api-端點)
3. [Android App 結構](#android-app-結構)
4. [主要功能特性](#主要功能特性)

---

## 數據庫結構

### 1. 用戶管理系統

#### Staff（員工）表
- `sid` - 員工ID（自增）
- `semail` - 員工郵箱（唯一）
- `spassword` - 密碼（加密存儲）
- `sname` - 員工名稱
- `srole` - 職位角色
- `stel` - 電話號碼
- `simageurl` - 大頭貼URL

#### Customer（顧客）表
- `cid` - 顧客ID（自增）
- `cname` - 顧客名稱
- `cpassword` - 密碼（加密存儲）
- `ctel` - 電話號碼
- `caddr` - 地址
- `company` - 公司名稱
- `cemail` - 郵箱（唯一）
- `cbirthday` - 生日（格式：MM-DD）
- `crole` - 角色（默認為'customer'）
- `cimageurl` - 頭像URL
- `coupon_point` - 優惠券點數

---

### 2. 優惠券系統（完整實現）

#### coupons（優惠券主表）
- `coupon_id` - 優惠券ID（自增）
- `points_required` - 兌換所需點數
- `type` - 類型：`cash`（現金）| `percent`（百分比）| `free_item`（免費項目）
- `discount_amount` - 折扣額度
- `item_category` - 適用分類
- `expiry_date` - 過期日期
- `is_active` - 是否激活（1/0）

#### coupon_translation（多語言翻譯）
- `translation_id` - 翻譯ID
- `coupon_id` - 優惠券ID（外鍵）
- `language_code` - 語言代碼（en/zh-CN/zh-TW）
- `title` - 優惠券標題
- `description` - 優惠券描述

#### coupon_rules（複雜規則引擎）
- `rule_id` - 規則ID
- `coupon_id` - 優惠券ID（外鍵）
- `applies_to` - 適用範圍：`whole_order`（全訂單）| `category`（分類）| `item`（特定項目）| `package`（套餐）
- `discount_type` - 折扣類型：`percent` | `cash` | `free_item`
- `discount_value` - 折扣值
- `min_spend` - 最低消費
- `max_discount` - 最高折扣額
- `per_customer_per_day` - 每位顧客每日限制
- `valid_dine_in` - 堂食是否適用
- `valid_takeaway` - 外賣是否適用
- `valid_delivery` - 外送是否適用
- `combine_with_other_discounts` - 是否可與其他折扣結合
- `birthday_only` - 是否限生日使用

#### coupon_terms（優惠券條款）
- `term_id` - 條款ID
- `coupon_id` - 優惠券ID（外鍵）
- `language_code` - 語言代碼
- `term_text` - 條款文本

#### coupon_point_history（點數變更記錄）
- `cph_id` - 記錄ID
- `cid` - 顧客ID（外鍵）
- `coupon_id` - 優惠券ID（外鍵）
- `delta` - 點數變化
- `resulting_points` - 結果點數
- `action` - 操作（兌換/使用/充值等）
- `note` - 備註
- `created_at` - 創建時間

#### coupon_redemptions（優惠券兌換記錄）
- `redemption_id` - 兌換記錄ID
- `coupon_id` - 優惠券ID（外鍵）
- `cid` - 顧客ID（外鍵）
- `redeemed_at` - 兌換時間
- `is_used` - 是否已使用（1/0）
- `used_at` - 使用時間

#### coupon_applicable_items（優惠券適用項目）
- `id` - ID
- `coupon_id` - 優惠券ID（外鍵）
- `item_id` - 菜品ID（外鍵）

#### coupon_applicable_categories（優惠券適用分類）
- `id` - ID
- `coupon_id` - 優惠券ID（外鍵）
- `category_id` - 分類ID（外鍵）

#### coupon_applicable_package（優惠券適用套餐）
- `id` - ID
- `coupon_id` - 優惠券ID（外鍵）
- `package_id` - 套餐ID（外鍵）

---

### 3. 菜單系統

#### menu_category（菜單分類）
5個主要分類：
1. Appetizers（開胃菜）
2. Soup（湯）
3. Main Courses（主菜）
4. Dessert（甜點）
5. Drink（飲品）

- `category_id` - 分類ID（主鍵）
- `category_name` - 分類名稱

#### menu_item（菜品主表）
18道菜品總數
- `item_id` - 菜品ID（主鍵）
- `category_id` - 所屬分類（外鍵）
- `item_price` - 價格（DECIMAL）
- `image_url` - 圖片URL
- `spice_level` - 辣度等級（0-5）
- `is_available` - 是否可用（布爾值）

#### menu_item_translation（菜品多語言翻譯）
- `translation_id` - 翻譯ID（主鍵）
- `item_id` - 菜品ID（外鍵）
- `language_code` - 語言代碼（en/zh-CN/zh-TW）
- `item_name` - 菜品名稱
- `item_description` - 菜品描述

#### tag（標籤系統）
22個標籤，分類如下：
- **Dietary（飲食）**：vegetarian（素食）
- **Characteristic（特性）**：refreshing（清爽）、classic（經典）、traditional（傳統）、streetfood（街食）
- **Protein（蛋白質）**：chicken（雞）、fish（魚）、beef（牛肉）、pork（豬肉）、tofu（豆腐）
- **Temperature（溫度）**：cold（冷）
- **Flavor（風味）**：spicy（辣）、sour（酸）、numbing（麻）、sweet（甜）、lemon（檸檬）、grape（葡萄）
- **Type（類型）**：noodles（麵條）、glutinous（糯米）、soda（汽水）
- **Cooking Method（烹飪方式）**：stirfry（炒）
- **Ingredient（材料）**：milk（奶）

- `tag_id` - 標籤ID（主鍵）
- `tag_name` - 標籤名稱（唯一）
- `tag_category` - 標籤分類
- `tag_bg_color` - 背景顏色

#### menu_tag（菜品-標籤映射）
- `item_id` - 菜品ID（外鍵）
- `tag_id` - 標籤ID（外鍵）

---

### 4. 自訂系統

#### customization_option_group（自訂選項組）
5個主要自訂組：
1. Spice Level（辣度）
2. Sugar Level（糖度）
3. Ice Level（冰量）
4. Milk Level（奶量）
5. Toppings（配菜）

- `group_id` - 組ID（自增）
- `group_name` - 組名稱（唯一）
- `group_type` - 組類型（spice/sugar/ice/milk/topping/other）

#### customization_option_value（自訂選項值）
- `value_id` - 值ID（自增）
- `group_id` - 所屬組（外鍵）
- `value_name` - 值名稱
- `display_order` - 顯示順序

**範例值：**
- Spice Level：Mild、Medium、Hot、Numbing
- Sugar Level：More Sweet、Less Sweet、No Sweet
- Ice Level：More Ice、Less Ice、No Ice
- Milk Level：More Milk、Less Milk、No Milk
- Toppings：Extra Sesame、Peanuts、Honey Drizzle、Chocolate Chips

#### item_customization_options（菜品自訂選項）
- `option_id` - 選項ID（自增）
- `item_id` - 菜品ID（外鍵）
- `group_id` - 選項組ID（外鍵）
- `max_selections` - 最多選擇數（默認1）
- `is_required` - 是否必選（0/1）

#### order_item_customizations（訂單項目自訂）
- `customization_id` - 自訂ID（自增）
- `oid` - 訂單ID（外鍵）
- `item_id` - 菜品ID（外鍵）
- `option_id` - 選項ID（外鍵）
- `group_id` - 組ID（外鍵）
- `selected_value_ids` - 已選值ID（JSON格式）
- `selected_values` - 已選值名稱（JSON格式）
- `text_value` - 文本值

---

### 5. 訂單系統

#### orders（訂單主表）
- `oid` - 訂單ID（自增）
- `odate` - 訂單日期時間
- `cid` - 顧客ID（外鍵）
- `ostatus` - 訂單狀態
- `note` - 備註
- `orderRef` - 訂單參考號（唯一）
- `coupon_id` - 應用的優惠券ID（外鍵）

#### order_items（訂單項目）
- `oid` - 訂單ID（外鍵）
- `item_id` - 菜品ID（外鍵）
- `qty` - 數量（默認1）
- `note` - 項目備註

#### order_coupons（訂單優惠券）
- `id` - ID（自增）
- `oid` - 訂單ID（外鍵）
- `coupon_id` - 優惠券ID（外鍵）
- `redemption_id` - 兌換記錄ID（外鍵）
- `discount_amount` - 折扣額度
- `applied_at` - 應用時間

#### order_packages（訂單套餐）
- `op_id` - ID（自增）
- `oid` - 訂單ID（外鍵）
- `package_id` - 套餐ID（外鍵）
- `qty` - 數量（默認1）
- `note` - 套餐備註

---

### 6. 套餐系統

#### menu_package（套餐主表）
3個預設套餐：
1. Double Set - 3種菜品 - HK$180
2. Four Person Set - 4種菜品 - HK$380
3. Business Set - 2種菜品 - HK$120

- `package_id` - 套餐ID（自增）
- `package_name` - 套餐名稱
- `num_of_type` - 菜品種類數
- `package_image_url` - 套餐圖片URL
- `amounts` - 套餐價格

#### package_type（套餐菜品類型）
- `type_id` - 類型ID（自增）
- `package_id` - 套餐ID（外鍵）
- `optional_quantity` - 可選數量

#### package_type_translation（套餐類型多語言）
- `type_translation_id` - 翻譯ID（自增）
- `type_id` - 類型ID（外鍵）
- `type_language_code` - 語言代碼
- `type_name` - 類型名稱（開胃菜、湯品、主菜、飲料）

#### package_dish（套餐內菜品）
- `package_id` - 套餐ID（外鍵）
- `type_id` - 類型ID（外鍵）
- `item_id` - 菜品ID（外鍵）
- `price_modifier` - 加價

#### order_package_item_customizations（套餐菜品自訂）✅ v4.6 新增
用於儲存套餐內各菜品的自訂選項（例：辣度、糖度、冰量等）
- `package_customization_id` - 自訂記錄ID（自增）
- `oid` - 訂單ID（外鍵）
- `op_id` - 套餐訂單ID（外鍵，來自order_packages）
- `package_id` - 套餐ID（外鍵）
- `item_id` - 套餐內菜品ID（外鍵）
- `group_id` - 自訂組ID（外鍵，如辣度組）
- `option_id` - 自訂選項ID（外鍵）
- `selected_value_ids` - 已選值ID（JSON格式：[1,2,3]）
- `selected_values` - 已選值名稱（JSON格式：["Mild","Less Sweet"]）
- `text_value` - 文本備註（如特殊要求）
- `created_at` - 創建時間

**索引優化**：
- `idx_order_package` - (oid, op_id) 快速查詢訂單的套餐
- `idx_package_item` - (package_id, item_id) 快速查詢套餐內菜品

---

### 7. 餐廳管理系統

#### seatingChart（座位表）
50張餐桌總數：
- 20張2人位餐桌
- 20張4人位餐桌
- 5張8人位餐桌

- `tid` - 桌號（自增）
- `capacity` - 容納人數
- `status` - 狀態（0=可用，1=已占用）

#### booking（預訂表）
- `bid` - 預訂ID（自增）
- `cid` - 顧客ID（外鍵，可為NULL）
- `bkcname` - 預訂人名稱
- `bktel` - 預訂人電話
- `tid` - 餐桌ID（外鍵）
- `bdate` - 預訂日期
- `btime` - 預訂時間
- `pnum` - 人數
- `purpose` - 預訂目的
- `remark` - 備註
- `status` - 預訂狀態（1=已確認，2=已就座，3=已完成，0=已取消）

#### table_orders（餐桌訂單狀態）
- `toid` - ID（自增）
- `table_number` - 桌號
- `oid` - 訂單ID（外鍵）
- `staff_id` - 員工ID（外鍵）
- `status` - 狀態：
  - `available`（可用）
  - `reserved`（已預訂）
  - `seated`（已就座）
  - `ordering`（點餐中）
  - `ready_to_pay`（準備結帳）
  - `paid`（已支付）
- `created_at` - 創建時間
- `updated_at` - 更新時間

---

### 8. 材料和食譜

#### materials（原料表）
7種原料：
1. Cucumber（黃瓜） - grams - 500g
2. Chicken（雞肉） - grams - 2000g
3. Soy Sauce（醬油） - ml - 1000ml
4. Chili Oil（辣油） - ml - 500ml
5. Rice（米） - grams - 10000g
6. Beef（牛肉） - grams - 1500g
7. Tofu（豆腐） - grams - 800g

- `mid` - 原料ID（自增）
- `mname` - 原料名稱
- `mcategory` - 原料分類（Vegetable/Meat/Condiment/Grain/Protein）
- `unit` - 單位（grams/ml）
- `mqty` - 庫存數量

#### recipe_materials（食譜原料）
- `id` - ID（自增）
- `item_id` - 菜品ID（外鍵）
- `mid` - 原料ID（外鍵）
- `quantity` - 所需數量

#### consumption_history（消耗記錄）
- `log_id` - 記錄ID（自增）
- `mid` - 原料ID（外鍵）
- `log_date` - 記錄日期
- `log_type` - 記錄類型（Deduction/Forecast/Reorder）
- `details` - 詳細信息
- `created_at` - 創建時間

---

## PHP API 端點

### API 基礎URL
`http://localhost/Database/projectapi/`

---

### 1. 用戶認證和管理

| 端點 | 方法 | 功能 | 參數 |
|-----|------|------|------|
| `register_user.php` | POST | 用戶註冊 | name, email, password, tel, addr |
| `get_customer.php` | GET | 獲取顧客信息 | cid |
| `get_staff.php` | GET | 獲取員工列表 | - |
| `update_birthday.php` | POST | 更新生日 | cid, birthday |
| `getBirthday.php` | GET | 獲取生日信息 | cid |

---

### 2. 菜單和商品

| 端點 | 方法 | 功能 | 參數 |
|-----|------|------|------|
| `list_products.php` | GET | 獲取所有產品 | - |
| `get_menuItems.php` | GET | 獲取菜單項目 | category_id |
| `get_package.php` | GET | 獲取單個套餐 | package_id |
| `get_packages.php` | GET | 獲取所有套餐 | - |

---

### 3. 自訂選項

| 端點 | 方法 | 功能 | 參數 |
|-----|------|------|------|
| `get_customization_options.php` | GET | 獲取菜品自訂選項 | item_id |

---

### 4. 優惠券系統

| 端點 | 方法 | 功能 | 參數 |
|-----|------|------|------|
| `getCoupons.php` | GET | 獲取所有優惠券 | - |
| `getMyCoupons.php` | GET | 獲取顧客已兌換優惠券 | cid |
| `getCouponDetail.php` | GET | 獲取優惠券詳情 | coupon_id |
| `getCouponPoints.php` | GET | 獲取單個優惠券點數 | coupon_id, cid |
| `get_customer_coupon_points.php` | GET | 獲取顧客優惠券點數 | cid |
| `getCouponHistory.php` | GET | 獲取優惠券兌換歷史 | cid |
| `redeemCoupon.php` | POST | 兌換優惠券 | cid, coupon_id |
| `useCoupon.php` | POST | 使用優惠券 | cid, coupon_id, oid |

---

### 5. 訂單系統

| 端點 | 方法 | 功能 | 參數 |
|-----|------|------|------|
| `place_order.php` | POST | 創建訂單 | cid, items, notes |
| `save_order.php` | POST | 保存訂單（含自訂） | oid, items, customizations |
| `get_orders.php` | GET | 獲取訂單列表 | cid |
| `get_orderItems.php` | GET | 獲取訂單項目 | oid |

---

### 6. 餐桌管理

| 端點 | 方法 | 功能 | 參數 |
|-----|------|------|------|
| `get_available_tables.php` | GET | 獲取可用餐桌 | bdate, btime, pnum |
| `get_tableOrders.php` | GET | 獲取餐桌訂單狀態 | table_id |

---

### 7. 預訂系統

| 端點 | 方法 | 功能 | 參數 |
|-----|------|------|------|
| `create_booking.php` | POST | 創建預訂 | cid, name, tel, tid, bdate, btime, pnum, purpose |

---

### 8. 支付系統

| 端點 | 方法 | 功能 | 參數 |
|-----|------|------|------|
| `create_payment_intent.php` | POST | 創建支付意圖 | amount, currency, oid |
| `payMoneyUrl.php` | POST | 獲取支付URL | amount, order_id |
| `payment-success.php` | GET/POST | 支付成功回調 | session_id |
| `payment-fail.php` | GET/POST | 支付失敗回調 | session_id |
| `payment-cancel.php` | GET/POST | 支付取消回調 | session_id |

---

### 9. 用戶媒體上傳

| 端點 | 方法 | 功能 | 參數 |
|-----|------|------|------|
| `save_customerImage.php` | POST | 上傳顧客圖片 | cid, image |
| `save_customerProfileImage.php` | POST | 上傳顧客頭像 | cid, image |
| `save_staffImage.php` | POST | 上傳員工圖片 | sid, image |
| `save_staffProfileImage.php` | POST | 上傳員工頭像 | sid, image |

---

### 10. 測試端點

| 端點 | 方法 | 功能 | 參數 |
|-----|------|------|------|
| `test_customization_save.php` | POST | 測試自訂選項保存 | - |
| `test_latest_order.php` | GET | 測試最新訂單 | cid |

---

## Android App 結構

### 技術棧
- **語言**：Java
- **框架**：Android SDK
- **網絡庫**：Retrofit2
- **JSON解析**：Gson
- **本地存儲**：SQLite（DatabaseHelper）
- **支付**：Stripe API

### 項目路徑
`c:\xampp\htdocs\newFolder\Android\YummyRestaurant\app\src\main\java\com\example\yummyrestaurant\`

---

### 1. 主入口

#### MainActivity.java
應用啟動入口，功能：
- 檢查用戶登入狀態
- 根據用戶角色路由：
  - Staff（員工）→ DashboardActivity
  - Customer（顧客）→ CustomerHomeActivity
- 申請Android 13+通知權限

---

### 2. 用戶認證（2個Activity）

#### LoginActivity.java
- 顧客和員工統一登入界面
- Email和密碼驗證
- 角色識別和保存

#### RegisterActivity.java
- 新用戶註冊
- 表單驗證
- 服務器數據同步

---

### 3. 顧客功能（22個Activity）

#### 主頁和導航
- **CustomerHomeActivity.java** - 顧客首頁，主導航中心

#### 菜單瀏覽
- **BrowseMenuActivity.java** - 瀏覽菜單，按分類展示
- **DishDetailActivity.java** - 菜品詳情頁面（價格、描述、圖片、標籤、評分）

#### 訂單流程
- **CustomizeDishActivity.java** - 菜品自訂（辣度、糖度、冰量、奶量、配菜選擇）
- **CartActivity.java** - 購物車（添加/刪除/修改數量和自訂）
- **PaymentActivity.java** - 支付頁面（Stripe集成）
- **TempPaymentActivity.java** - 臨時支付（測試/演示用）
- **OrderConfirmationActivity.java** - 訂單確認前總結
- **OrderHistoryActivity.java** - 歷史訂單查看
- **OrderTrackingActivity.java** - 訂單實時追蹤

#### 優惠券功能
- **CouponActivity.java** - 優惠券列表
- **CouponDetailActivity.java** - 優惠券詳情和條款
- **CouponHistoryActivity.java** - 優惠券使用歷史記錄
- **MyCouponsActivity.java** - 我的已兌換優惠券

#### 預訂系統
- **BookingActivity.java** - 餐桌預訂管理
- **ConfirmBookingActivity.java** - 預訂確認

#### 套餐管理
- **PackagesActivity.java** - 預設套餐列表
- **BuildSetMenuActivity.java** - 自訂套餐組合

#### 用戶中心
- **MembershipActivity.java** - 會員中心和等級
- **ProfileActivity.java** - 個人資料查看
- **EditProfileActivity.java** - 編輯個人信息
- **SettingsActivity.java** - 應用設定（語言、通知等）
- **SupportActivity.java** - 幫助和客服支援
- **StoreLocatorActivity.java** - 門店位置查詢
- **ReviewActivity.java** - 菜品評論和評分
- **WishlistActivity.java** - 收藏/願望單

#### 基類
- **BaseCustomerActivity.java** - 顧客Activity基類（通用方法和屬性）

---

### 4. 員工功能（4個Activity）

#### DashboardActivity.java
- 員工儀表板首頁
- 訂單統計和快速操作

#### TableOverviewActivity.java
- 所有餐桌狀態概覽
- 實時更新座位狀態

#### TableOrderDetailActivity.java
- 特定餐桌的訂單詳情
- 菜品列表和自訂信息

#### CheckInAndOutActivity.java
- 員工簽到/簽出
- 工作時間追蹤

---

### 5. API 服務（18個）

#### 核心服務

**ApiService.java**
```java
@GET("list_products.php")
Call<List<Product>> getProducts();

@GET("get_customization_options.php")
Call<CustomizationOptionsResponse> getCustomizationOptions(@Query("item_id") int itemId);

@GET("get_customer_coupon_points.php")
Call<CouponPointsResponse> getCouponPoints(@Query("cid") int cid);
```

#### 菜單相關
- **MenuApi.java** - 菜單數據API
- **ProductApi.java** - 產品列表API

#### 訂單和支付
- **OrderApiService.java** - 訂單CRUD操作
- **PaymentApiService.java** - 支付和支付狀態

#### 其他功能
- **TableApiService.java** - 餐桌管理
- **CouponApiService.java** - 優惠券操作

#### 認證
- **LoginCustomerApi.java** - 顧客登入
- **LoginStaffApi.java** - 員工登入
- **RegisterApi.java** - 用戶註冊

#### 媒體上傳
- **CustomerUploadApi.java** - 顧客圖片上傳
- **StaffUploadApi.java** - 員工圖片上傳

#### 基礎設施
- **RetrofitClient.java** - Retrofit2客戶端單例，配置基礎URL和攔截器
- **ApiConfig.java** - API全局配置

#### 響應模型
- **LoginResponse.java** - 登入響應（token、用戶信息）
- **RegisterResponse.java** - 註冊響應
- **PaymentIntentResponse.java** - 支付意圖響應（client_secret）
- **PaymentUrlResponse.java** - 支付URL響應

---

### 6. 數據模型（34個）

#### 用戶相關
- **User.java** - 用戶基類（cid、cname、cemail、ctel）
- **CartItem.java** - 購物車項目（item_id、qty、自訂信息）

#### 菜單相關
- **MenuItem.java** - 菜品（id、name、price、description、image_url、spice_level、tags、category_id）
- **MenuItemTranslation.java** - 菜品多語言翻譯
- **Product.java** - 產品（擴展MenuItem）

#### 訂單相關
- **Order.java** - 訂單（oid、odate、cid、ostatus、items、packages）
- **OrderItem.java** - 訂單項目（oid、item_id、qty、note）
- **OrderItemCustomization.java** - 訂單項自訂（group_id、selected_values）
- **OrderPackage.java** - 訂單套餐（package_id、qty、dishes）
- **OrderPackageDish.java** - 訂單套餐內菜品✅ v4.6 更新
  - `itemId` - 菜品ID
  - `name` - 菜品名稱
  - `price` - 菜品價格
  - `priceModifier` - 加價
  - `customizations` - ✅ **新增**：套餐菜品自訂列表（List<OrderItemCustomization>）

#### 套餐相關
- **SetMenu.java** - 自訂套餐
- **SetMenuResponse.java** - 自訂套餐API響應
- **PackagesResponse.java** - 套餐列表API響應
- **PackageType.java** - 套餐類型

#### 自訂相關
- **Customization.java** - 自訂選項容器
- **CustomizationOption.java** - 自訂選項詳情（group_id、group_name、values）
- **CustomizationOptionsResponse.java** - API響應

#### 優惠券相關
- **Coupon.java** - 優惠券基本信息
- **CouponDetailResponse.java** - 優惠券詳情API響應（包含規則、條款、描述）
- **CouponHistory.java** - 優惠券歷史容器
- **CouponHistoryItem.java** - 單條優惠券使用記錄
- **CouponHistoryResponse.java** - 歷史API響應
- **CouponListResponse.java** - 優惠券列表API響應
- **CouponPointResponse.java** - 單個優惠券點數響應
- **CouponPointsResponse.java** - 顧客總點數響應
- **MyCouponListResponse.java** - 我的優惠券API響應
- **RedeemCouponResponse.java** - 兌換優惠券API響應

#### 其他
- **Table.java** - 餐桌（tid、capacity、status）
- **TableOrder.java** - 餐桌訂單（toid、table_number、status）
- **Review.java** - 菜品評論
- **BirthdayResponse.java** - 生日API響應
- **GenericResponse.java** - 通用API響應（code、message）
- **UploadResponse.java** - 上傳API響應（url、status）

---

### 7. 本地數據庫

#### DatabaseHelper.java
- SQLite數據庫初始化和管理
- 本地緩存（可選）
- DAO層操作

---

### 8. 工具類

#### utils 資料夾
- **RoleManager.java** - 用戶角色管理

---

## 主要功能特性

### 📋 核心功能模塊

| 功能模塊 | 說明 | 相關表 | API端點 |
|--------|------|------|--------|
| **用戶管理** | 顧客和員工的登入/註冊，角色管理 | customer, staff | register_user.php, get_customer.php |
| **菜單系統** | 18道菜品，5個分類，22個標籤 | menu_category, menu_item, menu_tag | list_products.php, get_menuItems.php |
| **菜品自訂** | 多維度自訂（辣度、糖度、冰量、奶量、配菜） | customization_option_group, customization_option_value, item_customization_options | get_customization_options.php |
| **購物車** | 添加/修改/刪除菜品，保存自訂選項 | 本地SQLite | 前端管理 |
| **訂單管理** | 創建、查詢、追蹤訂單狀態 | orders, order_items, order_item_customizations | place_order.php, save_order.php, get_orders.php |
| **套餐系統** | 3個預設套餐或自訂組合 | menu_package, package_type, package_dish | get_packages.php, get_package.php |
| **優惠券系統** | 複雜規則引擎、點數兌換、多語言 | coupons, coupon_rules, coupon_redemptions, coupon_applicable_* | getCoupons.php, redeemCoupon.php, useCoupon.php |
| **預訂系統** | 線上預訂、餐桌管理、取消 | seatingChart, booking, table_orders | create_booking.php, get_available_tables.php |
| **支付系統** | Stripe集成，多支付方式 | orders, order_coupons | create_payment_intent.php, payment-*.php |
| **用戶資料** | 生日、地址、頭像上傳、個人信息編輯 | customer | update_birthday.php, save_customerProfileImage.php |
| **多語言支持** | 英文、繁體中文、簡體中文 | menu_item_translation, coupon_translation | 動態加載 |
| **員工功能** | 餐桌管理、訂單處理、簽到簽出 | staff, table_orders, table_number | get_staff.php, get_tableOrders.php |

---

### 🔄 主要業務流程

#### 1. 顧客訂餐流程
```
1. 登入/註冊 (LoginActivity/RegisterActivity)
   ↓
2. 瀏覽菜單 (BrowseMenuActivity)
   ↓
3. 查看菜品詳情 (DishDetailActivity)
   ↓
4. 自訂菜品 (CustomizeDishActivity)
   ↓
5. 添加到購物車 (CartActivity)
   ↓
6. 應用優惠券 (CouponActivity)
   ↓
7. 確認訂單 (OrderConfirmationActivity)
   ↓
8. 支付 (PaymentActivity) → Stripe
   ↓
9. 訂單確認和追蹤 (OrderTrackingActivity)
```

#### 2. 優惠券兌換流程
```
1. 顧客消費獲得點數
   ↓
2. 查看可兌換優惠券 (CouponActivity)
   ↓
3. 查看優惠券詳情和條款 (CouponDetailActivity)
   ↓
4. 兌換優惠券 (redeemCoupon.php)
   ↓
5. 訂餐時應用優惠券 (useCoupon.php)
   ↓
6. 系統計算折扣並應用
   ↓
7. 查看使用歷史 (CouponHistoryActivity)
```

#### 3. 餐桌預訂流程
```
1. 顧客進入預訂頁面 (BookingActivity)
   ↓
2. 選擇日期、時間、人數
   ↓
3. 系統查詢可用餐桌 (get_available_tables.php)
   ↓
4. 確認預訂 (ConfirmBookingActivity)
   ↓
5. 創建預訂記錄 (create_booking.php)
   ↓
6. 獲得預訂確認號
   ↓
7. 預訂日期到達時員工驗證 (TableOverviewActivity)
```

#### 4. 員工訂單管理流程
```
1. 員工簽到 (CheckInAndOutActivity)
   ↓
2. 查看餐桌概覽 (TableOverviewActivity)
   ↓
3. 查看特定餐桌訂單 (TableOrderDetailActivity)
   ↓
4. 標記訂單狀態（點餐、準備、結帳、已付）
   ↓
5. 清桌並更新狀態
   ↓
6. 員工簽出
```

---

### 🌍 多語言支持
- **英文 (en)** - 默認語言
- **繁體中文 (zh-TW)** - 香港和台灣用戶
- **簡體中文 (zh-CN)** - 中國大陸用戶

多語言應用於：
- 菜品名稱和描述
- 優惠券標題、描述和條款
- 套餐類型名稱

---

### 💳 支付集成
- **支付提供商**：Stripe
- **支付流程**：
  1. 創建支付意圖
  2. 獲取客戶端密鑰
  3. 前端調用Stripe SDK
  4. 支付成功/失敗回調

---

### 📊 數據特點

#### 規模
- **菜品總數**：18道
- **優惠券**：4個
- **套餐**：3個
- **餐桌**：50張
- **自訂組數**：5組
- **標籤**：22個
- **員工**：8位
- **顧客樣本**：5位

#### 設計特點
- ✅ 多語言支持
- ✅ 複雜的優惠券規則引擎
- ✅ 完整的自訂系統
- ✅ 套餐內菜品自訂（v4.6 新增）✅
- ✅ 餐廳運營功能
- ✅ 線上支付集成
- ✅ 用戶點數和兌換系統

---

## 📋 v4.6 新增功能：套餐菜品自訂系統

### 功能說明
顧客在訂購套餐時，可以對套餐內的每道菜品進行個性化自訂（如辣度、糖度、冰量等），系統完整支持自訂選項的保存和查詢。

### 實現架構

#### 1. 數據庫層（SQL v4.6）
**新增表**：`order_package_item_customizations`
```sql
CREATE TABLE order_package_item_customizations (
  package_customization_id INT PRIMARY KEY AUTO_INCREMENT,
  oid INT NOT NULL,              -- 訂單ID
  op_id INT NOT NULL,            -- 套餐訂單ID
  package_id INT NOT NULL,       -- 套餐ID
  item_id INT NOT NULL,          -- 套餐內菜品ID
  group_id INT NOT NULL,         -- 自訂組ID（辣度、糖度等）
  option_id INT NOT NULL,        -- 自訂選項ID
  selected_value_ids JSON,       -- 已選值ID
  selected_values JSON,          -- 已選值名稱
  text_value VARCHAR(500),       -- 文本備註
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (oid) REFERENCES orders(oid),
  FOREIGN KEY (op_id) REFERENCES order_packages(op_id),
  FOREIGN KEY (package_id) REFERENCES menu_package(package_id),
  FOREIGN KEY (item_id) REFERENCES menu_item(item_id),
  FOREIGN KEY (group_id) REFERENCES customization_option_group(group_id),
  FOREIGN KEY (option_id) REFERENCES item_customization_options(option_id)
);
```

#### 2. 後端 API（PHP v4.6）
**更新**：`save_order.php`

新增套餐菜品自訂保存邏輯：
```php
// 遍歷套餐內每個菜品
foreach ($packageItem['customizations'] as $pkg_custom) {
    // 提取自訂信息
    $pkg_option_id = $pkg_custom['option_id'];
    $pkg_group_id = $pkg_custom['group_id'];
    $pkg_selected_value_ids = json_encode($pkg_custom['selected_value_ids']);
    
    // 保存到 order_package_item_customizations
    $pkgCustomStmt->execute([
        $order_id, $order_package_id, $package_id,
        $item_id, $pkg_group_id, $pkg_option_id,
        $pkg_selected_value_ids, ...
    ]);
}
```

#### 3. Android 模型層（Java v4.6）
**更新**：`OrderPackageDish.java`

添加自訂列表字段：
```java
@SerializedName("customizations")
private List<OrderItemCustomization> customizations;

public List<OrderItemCustomization> getCustomizations() {
    return customizations;
}

public void setCustomizations(List<OrderItemCustomization> customizations) {
    this.customizations = customizations;
}
```

#### 4. 前端 JSON 數據結構
```json
{
  "cid": 1,
  "packages": [{
    "package_id": 1,
    "qty": 1,
    "dishes": [{
      "id": 5,
      "customizations": [
        {
          "group_id": 1,
          "option_id": 10,
          "selected_value_ids": [2],
          "selected_values": ["Medium"]
        }
      ]
    }]
  }]
}
```

### 使用流程

```
1. 顧客在 BuildSetMenuActivity 查看套餐菜品
   ↓
2. 點擊菜品觸發 CustomizeDishActivity
   ↓
3. 選擇自訂選項（辣度、糖度等）
   ↓
4. 返回結果到 BuildSetMenuActivity
   ↓
5. 自訂信息存入 OrderPackageDish.customizations
   ↓
6. 提交訂單時調用 save_order.php
   ↓
7. 後端保存到 order_package_item_customizations
```

### 數據查詢示例

**查詢訂單的套餐菜品自訂**：
```sql
SELECT opic.*, cov.value_name
FROM order_package_item_customizations opic
JOIN customization_option_value cov ON 
  JSON_CONTAINS(opic.selected_value_ids, CAST(cov.value_id AS JSON))
WHERE opic.oid = 1 AND opic.package_id = 1;
```

---

### 🔐 安全性考慮
- 密碼加密存儲
- API認證（建議實現token/JWT）
- 訂單參考號唯一性
- 優惠券有效期驗證
- 支付交易安全（Stripe處理）

---

## 總結

**Yummy Restaurant** 是一個功能完整的在線餐廳訂餐系統，包括：
- 🍽️ 完整的菜單和訂餐流程
- 🎫 高級優惠券和積分系統
- 📅 線上預訂和餐桌管理
- 💳 Stripe支付集成
- 👔 員工管理和訂單處理系統
- 🌐 多語言和多地區支持

---

**文檔版本**：v1.0  
**最後更新**：2025年12月27日  
**項目位置**：`c:\xampp\htdocs\newFolder\`

