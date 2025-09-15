package com.example.yummyrestaurant.model;

public class Review {
    private String userId;
    private String userEmail;
    private double rating;

    public Review() {}

    public Review(String userId, String userEmail, double rating) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.rating = rating;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
