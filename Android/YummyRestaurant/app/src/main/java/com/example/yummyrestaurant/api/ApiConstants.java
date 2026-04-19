package com.example.yummyrestaurant.api;

public class ApiConstants {
    private ApiConstants() {
    }

    public static String baseUrl() {
        return ApiConfig.getBaseUrl();
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