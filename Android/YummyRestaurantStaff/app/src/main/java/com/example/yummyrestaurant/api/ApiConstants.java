package com.example.yummyrestaurant.api;

public class ApiConstants {
    private ApiConstants() {
    }

    // Backward-compatible fields for legacy callers.
    public static String BASE_URL = ApiConfig.getBaseUrl();
    public static String LOGIN = BASE_URL + "staff_login.php";
    public static String GET_ORDERS = BASE_URL + "get_all_orders.php";
    public static String UPDATE_ORDER_STATUS = BASE_URL + "update_order_status.php";
    public static String GET_TABLE_STATUS = BASE_URL + "get_table_status.php";
    public static String RESERVE_TABLE = BASE_URL + "reserve_table.php";
    public static String GET_CASH_PAYMENT_TABLES = BASE_URL + "get_cash_payment_tables.php";
    public static String PROCESS_CASH_PAYMENT = BASE_URL + "process_cash_payment.php";

    public static void refresh() {
        BASE_URL = ApiConfig.getBaseUrl();
        LOGIN = BASE_URL + "staff_login.php";
        GET_ORDERS = BASE_URL + "get_all_orders.php";
        UPDATE_ORDER_STATUS = BASE_URL + "update_order_status.php";
        GET_TABLE_STATUS = BASE_URL + "get_table_status.php";
        RESERVE_TABLE = BASE_URL + "reserve_table.php";
        GET_CASH_PAYMENT_TABLES = BASE_URL + "get_cash_payment_tables.php";
        PROCESS_CASH_PAYMENT = BASE_URL + "process_cash_payment.php";
    }

    public static String baseUrl() {
        refresh();
        return BASE_URL;
    }

    public static String login() {
        return baseUrl() + "staff_login.php";
    }

    public static String getOrders() {
        return baseUrl() + "get_all_orders.php";
    }

    public static String updateOrderStatus() {
        return baseUrl() + "update_order_status.php";
    }

    public static String getTableStatus() {
        return baseUrl() + "get_table_status.php";
    }

    public static String reserveTable() {
        return baseUrl() + "reserve_table.php";
    }

    public static String getCashPaymentTables() {
        return baseUrl() + "get_cash_payment_tables.php";
    }

    public static String processCashPayment() {
        return baseUrl() + "process_cash_payment.php";
    }
}