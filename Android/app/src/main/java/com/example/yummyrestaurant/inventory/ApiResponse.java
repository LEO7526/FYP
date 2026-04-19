package com.example.yummyrestaurant.inventory;

public class ApiResponse<T> {
    public boolean success;
    public String message;
    public int generated_count;
    public T data;
}