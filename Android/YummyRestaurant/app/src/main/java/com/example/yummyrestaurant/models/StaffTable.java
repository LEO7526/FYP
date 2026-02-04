package com.example.yummyrestaurant.models;

public class StaffTable {
    private int id;
    private int status; // 0=Empty, 1=Available, 2=Occupied
    private String statusText; // "Available", "Occupied"
    private boolean isSelected = false; // 用於 UI 選取狀態

    public StaffTable(int id, int status, String statusText) {
        this.id = id;
        this.status = status;
        this.statusText = statusText;
    }

    // Default constructor for JSON parsing
    public StaffTable() {}

    public int getId() { return id; }
    public int getStatus() { return status; }
    public String getStatusText() { return statusText; }
    public boolean isSelected() { return isSelected; }
    
    public void setId(int id) { this.id = id; }
    public void setStatus(int status) { this.status = status; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public void setSelected(boolean selected) { isSelected = selected; }
}