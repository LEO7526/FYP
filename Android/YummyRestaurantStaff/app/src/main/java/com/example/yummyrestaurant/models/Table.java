package com.example.yummyrestaurant.models;

public class Table {
    private int id;
    private int status; // 0=Empty, 1=Occupied
    private String statusText; // "Available", "Seated"
    private boolean isSelected = false; // 用於 UI 選取狀態

    public Table(int id, int status, String statusText) {
        this.id = id;
        this.status = status;
        this.statusText = statusText;
    }

    public int getId() { return id; }
    public int getStatus() { return status; }
    public String getStatusText() { return statusText; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}