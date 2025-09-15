package com.example.fooddash;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.hardware.display.DisplayManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddash.adapter.MenuAdapter;
import com.example.fooddash.adapter.PopularAdapter;
import com.example.fooddash.adapter.RecommendedAdapter;
import com.example.fooddash.model.Food;
import com.example.fooddash.model.Menu;
import com.example.fooddash.model.Popular;
import com.example.fooddash.model.Recommended;
import com.example.fooddash.model.Restaurant;
import com.example.fooddash.model.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.*;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Home extends AppCompatActivity {

    private boolean isLocationDataReady = false;
    private int totalRestaurants = 0;
    private int processedRestaurants = 0;

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1001;

    Map<String, String> restaurantDurations = new HashMap<>();
    private RecyclerView popularRecyclerView, recommendedRecyclerView, menuRecyclerView;
    private PopularAdapter popularAdapter;
    private RecommendedAdapter recommendedAdapter;
    private MenuAdapter menuAdapter;

    private List<Popular> popularFood = new ArrayList<>();
    private List<Recommended> recommended = new ArrayList<>();
    private List<Menu> menus = new ArrayList<>();

    private User login;
    private String address = "";
    private boolean startup = true;
    private double lat1, long1;
    private double restLat, restLon;
    private String distance, duration;
    private String currentUser;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private FirebaseDatabase database;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show explanation dialog
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs location permission to calculate delivery time")
                        .setPositiveButton("OK", (dialog, which) ->
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSION_REQUEST_CODE))
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            fetchAndSaveAddress();
        }
    }

    private void updateUserAddress(String newAddress, double latitude, double longitude) {
        if (login != null) {
            // Update local user object
            login.setAddress(newAddress);
            login.setLatitude(latitude);
            login.setLongitude(longitude);

            // Use consistent key format for Firebase
            String userKey = login.getEmail().replace(".", ",");

            // Write updated user object to Firebase
            database.getReference("Users")
                    .child(userKey)
                    .setValue(login)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Address and location updated", Toast.LENGTH_SHORT).show();

                        // Reload user data to confirm update
                        loadUserData(userKey);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "User not loaded yet", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchAndSaveAddress() {

        Log.d("UserLocation", "fetchAndSaveAddress() called");

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1);

                            if (addresses != null && !addresses.isEmpty()) {
                                String newAddress = addresses.get(0).getAddressLine(0);
                                updateUserAddress(newAddress, location.getLatitude(), location.getLongitude());
                            } else {
                                Toast.makeText(this, "No address found for current location",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            Log.e("Location", "Geocoder error", e);
                            Toast.makeText(this, "Geocoder service unavailable",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Location unavailable. Please enable GPS",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Location", "Location fetch failed", e);
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView  shopButton = findViewById(R.id.imageView);
        shopButton.setEnabled(true);
        shopButton.setOnClickListener(v -> {
            Log.d("ClickTest", "Shop button clicked");
            shop(v);
        });

        database = FirebaseDatabase.getInstance();
        currentUser = getIntent().getStringExtra("username");
        startup = "startup".equals(getIntent().getAction());

        if (startup) loadUserData(currentUser);
        loadFoodData();
        setupRecyclerViews();
        startup = false;
    }


    private void loadFoodData() {
        DatabaseReference foodRef = database.getReference("Food");
        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                popularFood.clear();
                recommended.clear();
                menus.clear();

                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    Food food = foodSnapshot.getValue(Food.class);
                    if (food != null && food.getFoodType() != null) {
                        String foodType = food.getFoodType().toLowerCase().trim();

                        switch (foodType) {
                            case "popular":
                                popularFood.add(new Popular(
                                        food.getFoodName(),
                                        String.valueOf(food.getFoodPrice()),
                                        String.valueOf(food.getFoodRating()),
                                        String.valueOf(food.getDeliveryTime()),
                                        String.valueOf(food.getDeliveryCharges()),
                                        food.getFoodNote(),
                                        food.getFoodImageUrl()
                                ));
                                break;
                            case "recommended":
                                recommended.add(new Recommended(
                                        food.getFoodName(),
                                        String.valueOf(food.getFoodPrice()),
                                        String.valueOf(food.getFoodRating()),
                                        String.valueOf(food.getDeliveryTime()),
                                        String.valueOf(food.getDeliveryCharges()),
                                        food.getFoodNote(),
                                        food.getFoodImageUrl()
                                ));
                                break;
                            case "menu":
                                menus.add(new Menu(
                                        food.getFoodName(),
                                        String.valueOf(food.getFoodPrice()),
                                        String.valueOf(food.getFoodRating()),
                                        String.valueOf(food.getDeliveryTime()),
                                        String.valueOf(food.getDeliveryCharges()),
                                        food.getFoodNote(),
                                        food.getFoodImageUrl()
                                ));
                                break;
                            default:
                                Log.w("FoodData", "Unknown food type: " + foodType);
                        }
                    }
                }

                // Update adapters
                popularAdapter.notifyDataSetChanged();
                recommendedAdapter.notifyDataSetChanged();
                menuAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Food data load failed: " + error.getMessage());
                Toast.makeText(Home.this, "Failed to load food items", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editAddress:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1001);
                } else {
                    fetchAndSaveAddress();
                }
                return true;
            case R.id.logout:
                startActivity(new Intent(this, SignUpActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAndSaveAddress();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
    private void setupRecyclerViews() {
        popularRecyclerView = findViewById(R.id.popular_recycler);
        recommendedRecyclerView = findViewById(R.id.recommended_recycler);
        menuRecyclerView = findViewById(R.id.menu_recycler);

        popularAdapter = new PopularAdapter(this, popularFood);
        recommendedAdapter = new RecommendedAdapter(this, recommended);
        menuAdapter = new MenuAdapter(this, menus);

        setupRecyclerView(popularRecyclerView, popularAdapter, LinearLayoutManager.HORIZONTAL);
        setupRecyclerView(recommendedRecyclerView, recommendedAdapter, LinearLayoutManager.HORIZONTAL);
        setupRecyclerView(menuRecyclerView, menuAdapter, LinearLayoutManager.VERTICAL);
    }

    private void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter, int orientation) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, orientation, false));
        recyclerView.setAdapter(adapter);
    }

    private void loadUserData(String username) {
        DatabaseReference usersRef = database.getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                login = snapshot.child(username).getValue(User.class);
                if (login != null) {
                    address = login.getAddress();
                    lat1 = login.getLatitude();
                    long1 = login.getLongitude();

                    Log.d("UserLocation", "Address: " + address);
                    Log.d("UserLocation", "Saved Lat=" + lat1 + ", Lon=" + long1);

                    if (restLat != 0 && restLon != 0) {
                        fetchLocationData();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "User data load failed: " + error.getMessage());
            }
        });
    }
    private void fetchLocationDataForRestaurant(Restaurant restaurant, double userLat, double userLon) {
        Location origin = new Location("");
        origin.setLatitude(userLat);
        origin.setLongitude(userLon);

        Location destination = new Location("");
        destination.setLatitude(restaurant.getRestaurantLatitude());
        destination.setLongitude(restaurant.getRestaurantLongitude());

        String url = getDirectionsUrl(origin, destination);

        executor.execute(() -> {
            try {
                String jsonData = downloadUrl(url);
                JSONObject jsonObject = new JSONObject(jsonData);
                JSONObject route = jsonObject.getJSONArray("routes").optJSONObject(0);
                if (route != null) {
                    JSONObject leg = route.getJSONArray("legs").optJSONObject(0);
                    if (leg != null) {
                        String parsedDuration = leg.getJSONObject("duration").getString("text");

                        runOnUiThread(() -> {
                            restaurantDurations.put(restaurant.getRestaurantName(), parsedDuration);
                            processedRestaurants++;

                            // Check if all restaurants have been processed
                            if (processedRestaurants == totalRestaurants) {
                                isLocationDataReady = true;
                                duration = parsedDuration; // Or choose appropriate duration
                                ImageView shopButton = findViewById(R.id.imageView);
                                shopButton.setEnabled(true);
                                Toast.makeText(Home.this, "Delivery times calculated!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("ParseError", "Failed to parse JSON for " + restaurant.getRestaurantName(), e);
            }
        });
    }

    private List<Restaurant> restaurantList = new ArrayList<>();

    private void loadRestaurantData() {
        DatabaseReference restaurantRef = FirebaseDatabase.getInstance().getReference("Restaurant");

        restaurantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                restaurantList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Restaurant restaurant = postSnapshot.getValue(Restaurant.class);
                    if (restaurant != null) {
                        restaurantList.add(restaurant);

                        // 🔍 Log coordinates for debugging
                        Log.d("RestaurantLocation", restaurant.getRestaurantName() + ": "
                                + "Lat=" + restaurant.getRestaurantLatitude()
                                + ", Lon=" + restaurant.getRestaurantLongitude());
                    }
                }

                // Notify adapter or update UI here
//                restaurantAdapter.notifyDataSetChanged();
//hide it first
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to load restaurant data", error.toException());
            }
        });
    }

    private void fetchLocationData() {
        if (restLat == 0 || restLon == 0) {
            Toast.makeText(this, "Restaurant location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lat1 == 0 || long1 == 0) {
            Toast.makeText(this, "User location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Location origin = new Location("");
        origin.setLatitude(lat1);
        origin.setLongitude(long1);

        Location destination = new Location("");
        destination.setLatitude(restLat);
        destination.setLongitude(restLon);

        String url = getDirectionsUrl(origin, destination);

        executor.execute(() -> {
            try {
                String jsonData = downloadUrl(url);
                JSONObject jsonObject = new JSONObject(jsonData);
                JSONObject feature = jsonObject.getJSONArray("features").getJSONObject(0);
                JSONObject properties = feature.getJSONObject("properties");
                JSONObject summary = properties.getJSONObject("summary");

                double distanceMeters = summary.getDouble("distance");
                double durationSeconds = summary.getDouble("duration");

                String parsedDistance = String.format("%.2f km", distanceMeters / 1000);
                String parsedDuration = String.format("%.1f min", durationSeconds / 60);

                runOnUiThread(() -> {
                    distance = parsedDistance;
                    duration = parsedDuration;
                    Toast.makeText(Home.this, "Distance: " + distance + ", Duration: " + duration, Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                Log.e("ORSParseError", "Failed to parse OpenRouteService JSON", e);
                runOnUiThread(() -> Toast.makeText(Home.this, "Error parsing route data", Toast.LENGTH_SHORT).show());
            }
        });
    }



    private String getDirectionsUrl(Location origin, Location dest) {
        String strOrigin = "&start=" + origin.getLatitude() + "," + origin.getLongitude();
        String strDest = "&end=" + dest.getLatitude() + "," + dest.getLongitude();
        String key = "api_key=" + getResources().getString(R.string.api_key);
        return "https://api.openrouteservice.org/v2/directions/driving-car?" + key + strOrigin + strDest  ;
    }

    private String downloadUrl(String strUrl) throws IOException {
        StringBuilder sb = new StringBuilder();
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream iStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(iStream));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            if (reader != null) reader.close();
            if (urlConnection != null) urlConnection.disconnect();
        }

        return sb.toString();
    }

    public static LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        try {
            List<Address> addressList = coder.getFromLocationName(strAddress, 1);
            if (addressList == null || addressList.isEmpty()) return null;

            Address location = addressList.get(0);
            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            Log.e("Geocoder", "Failed to get location from address", e);
            return null;
        }
    }

    public void shop(View view) {
        if (!isLocationDataReady) {
            Toast.makeText(this, "Delivery times are still being calculated. Please wait...",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Home.this, ShoppingCart.class);
        intent.putExtra("duration", duration);
        startActivity(intent);
    }

    private void maybeLoadRestaurantData() {
        if (lat1 != 0 && long1 != 0) {
            loadRestaurantData();
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> Toast.makeText(Home.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                if (login != null) {

                    login.setAddress(address);
                    database.getReference("Users")
                            .child(login.getEmail().replace(".", ","))
                            .setValue(login);
                }else {
                    Toast.makeText(this, "User not loaded yet. Please try again.", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(this, "Address selected: " + address, Toast.LENGTH_SHORT).show();
            }
        }
    }
}