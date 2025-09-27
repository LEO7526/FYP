package com.example.yummyrestaurant.models;

public class User {
    private String id;
    private String name;
    private String email;
    private String phone;

    // Constructor
    public User(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
