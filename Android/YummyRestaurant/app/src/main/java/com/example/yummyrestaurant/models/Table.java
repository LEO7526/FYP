package com.example.yummyrestaurant.models;

public class Table {
    private int tid;
    private int capacity;


    public Table(int tid, int capacity) {
        this.tid = tid;
        this.capacity = capacity;
    }


    public int getTid() {
        return tid;
    }

    public int getCapacity() {
        return capacity;
    }
}
