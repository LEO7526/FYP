package com.example.fooddash.model;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class Restaurant {

    String restaurantName;
    String restaurantLogoUrl;
    String restaurantType;
    String restaurantOpenTime;
    String restaurantCloseTime;
    String restaurantAddress;
    Double restaurantLatitude;
    Double restaurantLongitude;

    public Restaurant() { }

    public Restaurant(String restaurantName, String restaurantLogoUrl, String restaurantType, String restaurantOpenTime, String restaurantCloseTime, String restaurantAddress, Double restaurantLatitude, Double restaurantLongitude) {
        this.restaurantName = restaurantName;
        this.restaurantLogoUrl = restaurantLogoUrl;
        this.restaurantType = restaurantType;
        this.restaurantOpenTime = restaurantOpenTime;
        this.restaurantCloseTime = restaurantCloseTime;
        this.restaurantAddress = restaurantAddress;
        this.restaurantLatitude = restaurantLatitude;
        this.restaurantLongitude = restaurantLongitude;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantLogoUrl() {
        return restaurantLogoUrl;
    }

    public void setRestaurantLogoUrl(String restaurantLogoUrl) {
        this.restaurantLogoUrl = restaurantLogoUrl;
    }

    public String getRestaurantType() {
        return restaurantType;
    }

    public void setRestaurantType(String restaurantType) {
        this.restaurantType = restaurantType;
    }

    public String getRestaurantOpenTime() {
        return restaurantOpenTime;
    }

    public void setRestaurantOpenTime(String restaurantOpenTime) {
        this.restaurantOpenTime = restaurantOpenTime;
    }

    public String getRestaurantCloseTime() {
        return restaurantCloseTime;
    }

    public void setRestaurantCloseTime(String restaurantCloseTime) {
        this.restaurantCloseTime = restaurantCloseTime;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public Double getRestaurantLatitude() {
        return restaurantLatitude;
    }

    public void setRestaurantLatitude(Double restaurantLatitude) {
        this.restaurantLatitude = restaurantLatitude;
    }

    public Double getRestaurantLongitude() {
        return restaurantLongitude;
    }

    public void setRestaurantLongitude(Double restaurantLongitude) {
        this.restaurantLongitude = restaurantLongitude;
    }


    public void loadRestaurants() {
        Restaurant r1 = new Restaurant(
                "Burger Haven",
                "https://example.com/images/burger_logo.png",
                "American",
                "10:00 AM",
                "10:00 PM",
                "Shop 5, G/F, 1 Sai Yeung Choi Street South, Mong Kok, Kowloon",
                22.3193,
                114.1694
        );

        Restaurant r2 = new Restaurant(
                "Pizza Palace",
                "https://example.com/images/pizza_logo.png",
                "Italian",
                "11:00 AM",
                "11:00 PM",
                "G/F, 88 Lockhart Road, Wan Chai, Hong Kong Island",
                22.2783,
                114.1747
        );

        Restaurant r3 = new Restaurant(
                "Ramen Republic",
                "https://example.com/images/ramen_logo.png",
                "Asian",
                "12:00 PM",
                "9:00 PM",
                "Shop 12, G/F, 2 Granville Road, Tsim Sha Tsui, Kowloon",
                22.3027,
                114.1772
        );

        List<Restaurant> restaurantList = Arrays.asList(r1, r2, r3);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference restRef = database.getReference("Restaurant");

        for (Restaurant r : restaurantList) {
            restRef.child(r.getRestaurantName().replace(" ", "_")).setValue(r)
                    .addOnSuccessListener(aVoid ->
                            Log.d("Firebase", "Uploaded: " + r.getRestaurantName()))
                    .addOnFailureListener(e ->
                            Log.e("Firebase", "Failed to upload " + r.getRestaurantName(), e));
        }
    }

}
