package com.example.yummyrestaurant.models;

public class Table {
    private int tid;
    private int capacity;
    private float x;              // X coordinate (percentage from left)
    private float y;              // Y coordinate (percentage from top)
    private String status;        // available, occupied, reserved
    private boolean is_available;
    private boolean suitable_for_booking;

    // Constructor for basic table info
    public Table(int tid, int capacity) {
        this.tid = tid;
        this.capacity = capacity;
        this.status = "available";
        this.is_available = true;
    }

    // Constructor for full table info (from seating chart)
    public Table(int tid, int capacity, float x, float y, String status, 
                 boolean is_available, boolean suitable_for_booking) {
        this.tid = tid;
        this.capacity = capacity;
        this.x = x;
        this.y = y;
        this.status = status;
        this.is_available = is_available;
        this.suitable_for_booking = suitable_for_booking;
    }

    // Getters
    public int getTid() {
        return tid;
    }

    public int getCapacity() {
        return capacity;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return is_available;
    }

    public boolean isSuitableForBooking() {
        return suitable_for_booking;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    public void setAvailable(boolean available) {
        is_available = available;
    }

    public void setSuitableForBooking(boolean suitable) {
        suitable_for_booking = suitable;
    }
}
