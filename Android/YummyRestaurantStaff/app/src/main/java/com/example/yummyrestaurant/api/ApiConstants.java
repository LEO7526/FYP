package com.example.yummyrestaurant.api;

public class ApiConstants {
    // 你的 IP (模擬器 10.0.2.2，實機請用電腦 IP)
    public static final String BASE_URL = "http://10.0.2.2/androidstaff_api/";

    public static final String LOGIN = BASE_URL + "staff_login.php";
    public static final String GET_ORDERS = BASE_URL + "get_all_orders.php";

    // ▼▼▼ 補上這一行 ▼▼▼
    public static final String UPDATE_ORDER_STATUS = BASE_URL + "update_order_status.php";

    // 如果你有做剛剛的桌位功能，這兩行也要補上：
    public static final String GET_TABLE_STATUS = BASE_URL + "get_table_status.php";
    public static final String RESERVE_TABLE = BASE_URL + "reserve_table.php";
}