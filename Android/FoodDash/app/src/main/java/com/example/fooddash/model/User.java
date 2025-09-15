package com.example.fooddash.model;

public class User {

    private String email;
    private String password;
    private String address;
    private double latitude;
    private double longitude;

    public User() {
    }

    public User(String email, String password, String address, double latitude, double longitude) {
        this.email = email;
        this.password = password;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}