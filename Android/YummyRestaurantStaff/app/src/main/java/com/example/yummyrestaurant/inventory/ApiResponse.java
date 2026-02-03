package com.example.yummyrestaurant.inventory;

public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;
}