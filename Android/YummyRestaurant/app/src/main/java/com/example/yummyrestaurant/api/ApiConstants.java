package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.BuildConfig;

public class ApiConstants {
    public static final String BASE_URL = BuildConfig.API_DEFAULT_BASE_URL;

    public static final String LOGIN = BASE_URL + "staff_login.php";
    public static final String GET_ORDERS = BASE_URL + "get_all_orders.php";

    // ▼▼▼ 補上這一行 ▼▼▼
    public static final String UPDATE_ORDER_STATUS = BASE_URL + "update_order_status.php";

    // 如果你有做剛剛的桌位功能，這兩行也要補上：
    public static final String GET_TABLE_STATUS = BASE_URL + "get_table_status.php";
    public static final String RESERVE_TABLE = BASE_URL + "reserve_table.php";
    
    // Cash Payment Management APIs
    public static final String GET_CASH_PAYMENT_TABLES = BASE_URL + "get_cash_payment_tables.php";
    public static final String PROCESS_CASH_PAYMENT = BASE_URL + "process_cash_payment.php";
}